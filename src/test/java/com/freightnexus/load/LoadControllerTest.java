package com.freightnexus.load;

import com.freightnexus.BaseIntegrationTest;
import com.freightnexus.capacity.CapacityDTO;
import com.freightnexus.contract.ContractDTO;
import com.freightnexus.driver.DriverDTO;
import com.freightnexus.lane.LaneDTO;
import com.freightnexus.rateplan.PricingModel;
import com.freightnexus.rateplan.RatePlanDTO;
import com.freightnexus.shipment.ShipmentDTO;
import com.freightnexus.vehicle.VehicleDTO;
import com.freightnexus.vehicle.VehicleType;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class LoadControllerTest extends BaseIntegrationTest {

    @Test
    void fullFreightBookingFlow() {
        // Carrier setup
        createCarrier("FastFreight Co", "carrier@test.com");
        String carrierToken = loginToken("carrier@test.com", "pass123");

        Long vehicleId = restTemplate.exchange("/vehicles", HttpMethod.POST,
                auth(new VehicleDTO.Request("MH-01-AB-1234", VehicleType.BOX_TRUCK,
                        new BigDecimal("5000"), new BigDecimal("20")), carrierToken),
                VehicleDTO.Response.class).getBody().id();

        // Add driver
        Long driverId = restTemplate.exchange("/drivers", HttpMethod.POST,
                auth(new DriverDTO.Request("Raj Kumar", "raj@test.com", "dpass123",
                        "DL-MH-12345", LocalDate.of(2028, 1, 1)), carrierToken),
                DriverDTO.Response.class).getBody().id();

        // Set capacity for pickup date
        LocalDate pickupDate = LocalDate.of(2026, 9, 1);
        restTemplate.exchange("/vehicles/" + vehicleId + "/capacity", HttpMethod.PUT,
                auth(new CapacityDTO.Request(pickupDate, new BigDecimal("5000"), new BigDecimal("20")), carrierToken),
                CapacityDTO.Response.class);

        // Create lane and rate plan
        Long laneId = restTemplate.exchange("/lanes", HttpMethod.POST,
                auth(new LaneDTO.Request("Mumbai", "India",
                        new BigDecimal("19.0760"), new BigDecimal("72.8777"),
                        "Delhi", "India",
                        new BigDecimal("28.6139"), new BigDecimal("77.2090"),
                        new BigDecimal("1400"), new BigDecimal("8")), carrierToken),
                LaneDTO.Response.class).getBody().id();

        Long ratePlanId = restTemplate.exchange("/lanes/" + laneId + "/rate-plans", HttpMethod.POST,
                auth(new RatePlanDTO.Request("Standard Rate", PricingModel.PER_KG, new BigDecimal("2.50")), carrierToken),
                RatePlanDTO.Response.class).getBody().id();

        // Shipper setup
        Long shipperId = createShipper("Acme Electronics", "shipper@test.com");
        String shipperToken = loginToken("shipper@test.com", "pass123");

        // Contract: carrier offers, activates
        Long contractId = restTemplate.exchange("/contracts", HttpMethod.POST,
                auth(new ContractDTO.Request(shipperId, LocalDate.of(2026, 1, 1), null), carrierToken),
                ContractDTO.Response.class).getBody().id();
        restTemplate.exchange("/contracts/" + contractId + "/activate", HttpMethod.PUT, auth(carrierToken), ContractDTO.Response.class);

        // Shipper creates shipment and books load
        Long shipmentId = restTemplate.exchange("/shipments", HttpMethod.POST,
                auth(new ShipmentDTO.Request("Electronics Batch A", new BigDecimal("500"),
                        new BigDecimal("2"), false, new BigDecimal("150000")), shipperToken),
                ShipmentDTO.Response.class).getBody().id();

        ResponseEntity<LoadDTO.Response> loadResp = restTemplate.exchange("/loads", HttpMethod.POST,
                auth(new LoadDTO.Request(shipmentId, vehicleId, driverId, ratePlanId, pickupDate), shipperToken),
                LoadDTO.Response.class);

        assertThat(loadResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(loadResp.getBody().status()).isEqualTo(LoadStatus.PENDING);
        assertThat(loadResp.getBody().reference()).startsWith("LD-");
        // 500 kg × ₹2.50/kg = ₹1,250
        assertThat(loadResp.getBody().totalCharge()).isEqualByComparingTo(new BigDecimal("1250.00"));

        Long loadId = loadResp.getBody().id();

        // Check status history
        LoadDTO.HistoryEntry[] history = restTemplate.exchange("/loads/" + loadId + "/history",
                HttpMethod.GET, auth(shipperToken), LoadDTO.HistoryEntry[].class).getBody();
        assertThat(history).hasSize(1);
        assertThat(history[0].toStatus()).isEqualTo("PENDING");
    }

    @Test
    void insufficientCapacityReturns409() {
        createCarrier("SmallFleet", "small@test.com");
        String carrierToken = loginToken("small@test.com", "pass123");

        Long vehicleId = restTemplate.exchange("/vehicles", HttpMethod.POST,
                auth(new VehicleDTO.Request("DL-01-CD-5678", VehicleType.VAN,
                        new BigDecimal("100"), new BigDecimal("1")), carrierToken),
                VehicleDTO.Response.class).getBody().id();

        Long driverId = restTemplate.exchange("/drivers", HttpMethod.POST,
                auth(new DriverDTO.Request("Driver One", "driver@test.com", "dpass",
                        "LIC-001", LocalDate.of(2027, 6, 1)), carrierToken),
                DriverDTO.Response.class).getBody().id();

        LocalDate pickupDate = LocalDate.of(2026, 10, 1);
        // Only 50 kg capacity available
        restTemplate.exchange("/vehicles/" + vehicleId + "/capacity", HttpMethod.PUT,
                auth(new CapacityDTO.Request(pickupDate, new BigDecimal("50"), new BigDecimal("1")), carrierToken),
                CapacityDTO.Response.class);

        Long laneId = restTemplate.exchange("/lanes", HttpMethod.POST,
                auth(new LaneDTO.Request("Chennai", "India",
                        new BigDecimal("13.0827"), new BigDecimal("80.2707"),
                        "Bangalore", "India",
                        new BigDecimal("12.9716"), new BigDecimal("77.5946"),
                        new BigDecimal("346"), new BigDecimal("6")), carrierToken),
                LaneDTO.Response.class).getBody().id();

        Long ratePlanId = restTemplate.exchange("/lanes/" + laneId + "/rate-plans", HttpMethod.POST,
                auth(new RatePlanDTO.Request("Express", PricingModel.FLAT, new BigDecimal("5000")), carrierToken),
                RatePlanDTO.Response.class).getBody().id();

        Long shipperId = createShipper("Overweight Shipper", "over@test.com");
        String shipperToken = loginToken("over@test.com", "pass123");

        Long contractId = restTemplate.exchange("/contracts", HttpMethod.POST,
                auth(new ContractDTO.Request(shipperId, LocalDate.of(2026, 1, 1), null), carrierToken),
                ContractDTO.Response.class).getBody().id();
        restTemplate.exchange("/contracts/" + contractId + "/activate", HttpMethod.PUT, auth(carrierToken), ContractDTO.Response.class);

        // 200 kg shipment but only 50 kg available
        Long shipmentId = restTemplate.exchange("/shipments", HttpMethod.POST,
                auth(new ShipmentDTO.Request("Heavy Cargo", new BigDecimal("200"),
                        new BigDecimal("1"), false, null), shipperToken),
                ShipmentDTO.Response.class).getBody().id();

        ResponseEntity<String> response = restTemplate.exchange("/loads", HttpMethod.POST,
                auth(new LoadDTO.Request(shipmentId, vehicleId, driverId, ratePlanId, pickupDate), shipperToken),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}
