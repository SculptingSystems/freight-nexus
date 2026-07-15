package com.freightnexus.partner;

import com.freightnexus.auth.AuthDTO;
import com.freightnexus.driver.DriverRepository;
import com.freightnexus.hos.HOSWindowRepository;
import com.freightnexus.capacity.VehicleCapacityRepository;
import com.freightnexus.contract.ShipperContractRepository;
import com.freightnexus.load.LoadRepository;
import com.freightnexus.load.LoadStatusHistoryRepository;
import com.freightnexus.shipment.ShipmentRepository;
import com.freightnexus.tracking.TrackingRepository;
import com.freightnexus.vehicle.VehicleRepository;
import com.freightnexus.webhook.WebhookOutboxRepository;
import com.freightnexus.rateplan.RatePlanRepository;
import com.freightnexus.lane.LaneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PartnerControllerTest {

    @Autowired TestRestTemplate restTemplate;
    @Autowired WebhookOutboxRepository webhookOutboxRepository;
    @Autowired TrackingRepository trackingRepository;
    @Autowired LoadStatusHistoryRepository loadStatusHistoryRepository;
    @Autowired LoadRepository loadRepository;
    @Autowired ShipmentRepository shipmentRepository;
    @Autowired VehicleCapacityRepository vehicleCapacityRepository;
    @Autowired ShipperContractRepository contractRepository;
    @Autowired HOSWindowRepository hosWindowRepository;
    @Autowired RatePlanRepository ratePlanRepository;
    @Autowired LaneRepository laneRepository;
    @Autowired DriverRepository driverRepository;
    @Autowired VehicleRepository vehicleRepository;
    @Autowired PartnerRepository partnerRepository;

    private String token;

    @BeforeEach
    void setUp() {
        webhookOutboxRepository.deleteAll();
        trackingRepository.deleteAll();
        loadStatusHistoryRepository.deleteAll();
        loadRepository.deleteAll();
        shipmentRepository.deleteAll();
        vehicleCapacityRepository.deleteAll();
        contractRepository.deleteAll();
        hosWindowRepository.deleteAll();
        ratePlanRepository.deleteAll();
        laneRepository.deleteAll();
        driverRepository.deleteAll();
        vehicleRepository.deleteAll();
        partnerRepository.deleteAll();

        restTemplate.postForEntity("/partners",
                new PartnerDTO.Request("Test Carrier", "carrier@test.com", PartnerType.CARRIER, "pass123"),
                PartnerDTO.Response.class);
        token = restTemplate.postForObject("/auth/login",
                new AuthDTO.LoginRequest("carrier@test.com", "pass123"),
                AuthDTO.TokenResponse.class).token();
    }

    @Test
    void registerPartnerAndLogin() {
        PartnerDTO.Response partner = restTemplate.postForEntity("/partners",
                new PartnerDTO.Request("Acme Shipping", "acme@test.com", PartnerType.SHIPPER, "pass456"),
                PartnerDTO.Response.class).getBody();

        assertThat(partner.type()).isEqualTo(PartnerType.SHIPPER);
        assertThat(partner.status()).isEqualTo(PartnerStatus.ACTIVE);

        AuthDTO.TokenResponse loginResponse = restTemplate.postForObject("/auth/login",
                new AuthDTO.LoginRequest("acme@test.com", "pass456"),
                AuthDTO.TokenResponse.class);

        assertThat(loginResponse.token()).isNotBlank();
        assertThat(loginResponse.role()).isEqualTo("SHIPPER");
    }

    @Test
    void listPartnersRequiresAuth() {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        ResponseEntity<String> response = restTemplate.exchange("/partners", HttpMethod.GET,
                new HttpEntity<>(h), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("content");
    }

    @Test
    void listPartnersWithoutTokenReturns401() {
        ResponseEntity<String> response = restTemplate.getForEntity("/partners", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
