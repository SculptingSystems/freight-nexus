package com.freightnexus;

import com.freightnexus.auth.AuthDTO;
import com.freightnexus.capacity.VehicleCapacityRepository;
import com.freightnexus.contract.ShipperContractRepository;
import com.freightnexus.driver.DriverRepository;
import com.freightnexus.hos.HOSWindowRepository;
import com.freightnexus.lane.LaneRepository;
import com.freightnexus.load.LoadRepository;
import com.freightnexus.load.LoadStatusHistoryRepository;
import com.freightnexus.partner.PartnerDTO;
import com.freightnexus.partner.PartnerRepository;
import com.freightnexus.partner.PartnerType;
import com.freightnexus.rateplan.RatePlanRepository;
import com.freightnexus.shipment.ShipmentRepository;
import com.freightnexus.tracking.TrackingRepository;
import com.freightnexus.vehicle.VehicleRepository;
import com.freightnexus.webhook.WebhookOutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIntegrationTest {

    @Autowired protected TestRestTemplate restTemplate;
    @Autowired protected WebhookOutboxRepository webhookOutboxRepository;
    @Autowired protected TrackingRepository trackingRepository;
    @Autowired protected LoadStatusHistoryRepository loadStatusHistoryRepository;
    @Autowired protected LoadRepository loadRepository;
    @Autowired protected ShipmentRepository shipmentRepository;
    @Autowired protected VehicleCapacityRepository vehicleCapacityRepository;
    @Autowired protected ShipperContractRepository contractRepository;
    @Autowired protected HOSWindowRepository hosWindowRepository;
    @Autowired protected RatePlanRepository ratePlanRepository;
    @Autowired protected LaneRepository laneRepository;
    @Autowired protected DriverRepository driverRepository;
    @Autowired protected VehicleRepository vehicleRepository;
    @Autowired protected PartnerRepository partnerRepository;

    @BeforeEach
    void cleanAll() {
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
    }

    protected String loginToken(String email, String password) {
        return restTemplate.postForObject("/auth/login",
                new AuthDTO.LoginRequest(email, password),
                AuthDTO.TokenResponse.class).token();
    }

    protected String driverLoginToken(String email, String password) {
        return restTemplate.postForObject("/auth/driver-login",
                new AuthDTO.LoginRequest(email, password),
                AuthDTO.TokenResponse.class).token();
    }

    protected Long createCarrier(String name, String email) {
        return restTemplate.postForEntity("/partners",
                new PartnerDTO.Request(name, email, PartnerType.CARRIER, "pass123"),
                PartnerDTO.Response.class).getBody().id();
    }

    protected Long createShipper(String name, String email) {
        return restTemplate.postForEntity("/partners",
                new PartnerDTO.Request(name, email, PartnerType.SHIPPER, "pass123"),
                PartnerDTO.Response.class).getBody().id();
    }

    protected <T> HttpEntity<T> auth(T body, String token) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        return new HttpEntity<>(body, h);
    }

    protected HttpEntity<Void> auth(String token) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(token);
        return new HttpEntity<>(h);
    }
}
