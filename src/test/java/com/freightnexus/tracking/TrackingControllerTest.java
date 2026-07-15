package com.freightnexus.tracking;

import com.freightnexus.BaseIntegrationTest;
import com.freightnexus.capacity.CapacityDTO;
import com.freightnexus.contract.ContractDTO;
import com.freightnexus.driver.DriverDTO;
import com.freightnexus.lane.LaneDTO;
import com.freightnexus.load.LoadDTO;
import com.freightnexus.load.LoadStatus;
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
import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class TrackingControllerTest extends BaseIntegrationTest {

    @Test
    void gpsTrackingAndETACalculation() {
        // Full setup
        createCarrier("TrackCo", "carrier@track.com");
        String carrierToken = loginToken("carrier@track.com", "pass123");

        Long vehicleId = restTemplate.exchange("/vehicles", HttpMethod.POST,
                auth(new VehicleDTO.Request("GJ-01-AA-0001", VehicleType.TRUCK_18_WHEELER,
                        new BigDecimal("20000"), new BigDecimal("80")), carrierToken),
                VehicleDTO.Response.class).getBody().id();

        Long driverId = restTemplate.exchange("/drivers", HttpMethod.POST,
                auth(new DriverDTO.Request("Suresh Patel", "suresh@track.com", "dpass",
                        "GJ-LIC-999", LocalDate.of(2029, 1, 1)), carrierToken),
                DriverDTO.Response.class).getBody().id();

        String driverToken = driverLoginToken("suresh@track.com", "dpass");

        LocalDate pickup = LocalDate.of(2026, 11, 1);
        restTemplate.exchange("/vehicles/" + vehicleId + "/capacity", HttpMethod.PUT,
                auth(new CapacityDTO.Request(pickup, new BigDecimal("20000"), new BigDecimal("80")), carrierToken),
                CapacityDTO.Response.class);

        Long laneId = restTemplate.exchange("/lanes", HttpMethod.POST,
                auth(new LaneDTO.Request("Ahmedabad", "India",
                        new BigDecimal("23.0225"), new BigDecimal("72.5714"),
                        "Mumbai", "India",
                        new BigDecimal("19.0760"), new BigDecimal("72.8777"),
                        new BigDecimal("530"), new BigDecimal("8")), carrierToken),
                LaneDTO.Response.class).getBody().id();

        Long ratePlanId = restTemplate.exchange("/lanes/" + laneId + "/rate-plans", HttpMethod.POST,
                auth(new RatePlanDTO.Request("Flat Rate", PricingModel.FLAT, new BigDecimal("25000")), carrierToken),
                RatePlanDTO.Response.class).getBody().id();

        Long shipperId = createShipper("Gujarat Textiles", "textiles@test.com");
        String shipperToken = loginToken("textiles@test.com", "pass123");

        Long contractId = restTemplate.exchange("/contracts", HttpMethod.POST,
                auth(new ContractDTO.Request(shipperId, LocalDate.of(2026, 1, 1), null), carrierToken),
                ContractDTO.Response.class).getBody().id();
        restTemplate.exchange("/contracts/" + contractId + "/activate", HttpMethod.PUT, auth(carrierToken), ContractDTO.Response.class);

        Long shipmentId = restTemplate.exchange("/shipments", HttpMethod.POST,
                auth(new ShipmentDTO.Request("Cotton Bales", new BigDecimal("5000"),
                        new BigDecimal("25"), false, new BigDecimal("500000")), shipperToken),
                ShipmentDTO.Response.class).getBody().id();

        Long loadId = restTemplate.exchange("/loads", HttpMethod.POST,
                auth(new LoadDTO.Request(shipmentId, vehicleId, driverId, ratePlanId, pickup), shipperToken),
                LoadDTO.Response.class).getBody().id();

        // Driver posts GPS event (mid-route between Ahmedabad and Mumbai)
        ResponseEntity<TrackingDTO.EventResponse> pingResp = restTemplate.exchange(
                "/loads/" + loadId + "/tracking", HttpMethod.POST,
                auth(new TrackingDTO.EventRequest(
                        new BigDecimal("21.1702"),  // Vadodara area
                        new BigDecimal("72.8311"),
                        new BigDecimal("75.0"),     // 75 km/h
                        new BigDecimal("180.0"),    // heading south
                        Instant.now()
                ), driverToken),
                TrackingDTO.EventResponse.class);

        assertThat(pingResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(pingResp.getBody().latitude()).isEqualByComparingTo(new BigDecimal("21.1702"));

        // Shipper retrieves live position + ETA
        ResponseEntity<TrackingDTO.LivePosition> liveResp = restTemplate.exchange(
                "/loads/" + loadId + "/tracking/live", HttpMethod.GET, auth(shipperToken),
                TrackingDTO.LivePosition.class);

        assertThat(liveResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(liveResp.getBody().distanceRemainingKm()).isGreaterThan(0);
        assertThat(liveResp.getBody().etaHours()).isGreaterThan(0);
        assertThat(liveResp.getBody().reference()).startsWith("LD-");
    }

    @Test
    void duplicateGpsPingIsIdempotent() {
        // Minimal setup to get a load
        createCarrier("IdempCo", "idemp@test.com");
        String carrierToken = loginToken("idemp@test.com", "pass123");

        Long vehicleId = restTemplate.exchange("/vehicles", HttpMethod.POST,
                auth(new VehicleDTO.Request("RJ-01-BB-2222", VehicleType.BOX_TRUCK,
                        new BigDecimal("3000"), new BigDecimal("15")), carrierToken),
                VehicleDTO.Response.class).getBody().id();

        Long driverId = restTemplate.exchange("/drivers", HttpMethod.POST,
                auth(new DriverDTO.Request("Driver Two", "d2@test.com", "dpass",
                        "RJ-LIC-002", LocalDate.of(2027, 1, 1)), carrierToken),
                DriverDTO.Response.class).getBody().id();

        String driverToken = driverLoginToken("d2@test.com", "dpass");
        LocalDate pickup = LocalDate.of(2026, 12, 1);

        restTemplate.exchange("/vehicles/" + vehicleId + "/capacity", HttpMethod.PUT,
                auth(new CapacityDTO.Request(pickup, new BigDecimal("3000"), new BigDecimal("15")), carrierToken),
                CapacityDTO.Response.class);

        Long laneId = restTemplate.exchange("/lanes", HttpMethod.POST,
                auth(new LaneDTO.Request("Jaipur", "India",
                        new BigDecimal("26.9124"), new BigDecimal("75.7873"),
                        "Delhi", "India",
                        new BigDecimal("28.6139"), new BigDecimal("77.2090"),
                        new BigDecimal("280"), new BigDecimal("5")), carrierToken),
                LaneDTO.Response.class).getBody().id();

        Long ratePlanId = restTemplate.exchange("/lanes/" + laneId + "/rate-plans", HttpMethod.POST,
                auth(new RatePlanDTO.Request("Direct", PricingModel.FLAT, new BigDecimal("8000")), carrierToken),
                RatePlanDTO.Response.class).getBody().id();

        Long shipperId = createShipper("Rajasthan Gems", "gems@test.com");
        String shipperToken = loginToken("gems@test.com", "pass123");

        Long contractId = restTemplate.exchange("/contracts", HttpMethod.POST,
                auth(new ContractDTO.Request(shipperId, LocalDate.of(2026, 1, 1), null), carrierToken),
                ContractDTO.Response.class).getBody().id();
        restTemplate.exchange("/contracts/" + contractId + "/activate", HttpMethod.PUT, auth(carrierToken), ContractDTO.Response.class);

        Long shipmentId = restTemplate.exchange("/shipments", HttpMethod.POST,
                auth(new ShipmentDTO.Request("Gemstones", new BigDecimal("50"),
                        new BigDecimal("0.5"), false, new BigDecimal("1000000")), shipperToken),
                ShipmentDTO.Response.class).getBody().id();

        Long loadId = restTemplate.exchange("/loads", HttpMethod.POST,
                auth(new LoadDTO.Request(shipmentId, vehicleId, driverId, ratePlanId, pickup), shipperToken),
                LoadDTO.Response.class).getBody().id();

        Instant timestamp = Instant.parse("2026-12-01T06:00:00Z");
        TrackingDTO.EventRequest ping = new TrackingDTO.EventRequest(
                new BigDecimal("27.5000"), new BigDecimal("76.5000"),
                new BigDecimal("60.0"), null, timestamp);

        // Send same ping twice
        restTemplate.exchange("/loads/" + loadId + "/tracking", HttpMethod.POST, auth(ping, driverToken), TrackingDTO.EventResponse.class);
        ResponseEntity<TrackingDTO.EventResponse> second = restTemplate.exchange(
                "/loads/" + loadId + "/tracking", HttpMethod.POST, auth(ping, driverToken), TrackingDTO.EventResponse.class);

        // Both succeed, idempotent
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
}
