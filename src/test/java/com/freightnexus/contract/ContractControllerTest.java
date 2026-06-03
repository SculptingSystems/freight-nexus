package com.freightnexus.contract;

import com.freightnexus.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ContractControllerTest extends BaseIntegrationTest {

    @Test
    void carrierCreatesAndActivatesContract() {
        createCarrier("Allied Logistics", "allied@test.com");
        String carrierToken = loginToken("allied@test.com", "pass123");

        Long shipperId = createShipper("Pacific Traders", "pacific@test.com");
        String shipperToken = loginToken("pacific@test.com", "pass123");

        ResponseEntity<ContractDTO.Response> created = restTemplate.exchange("/contracts", HttpMethod.POST,
                auth(new ContractDTO.Request(shipperId, LocalDate.of(2026, 1, 1), null), carrierToken),
                ContractDTO.Response.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody().status()).isEqualTo(ContractStatus.DRAFT);
        Long contractId = created.getBody().id();

        restTemplate.exchange("/contracts/" + contractId + "/activate", HttpMethod.PUT, auth(carrierToken), ContractDTO.Response.class);

        ContractDTO.Response[] shipperContracts = restTemplate.exchange("/contracts", HttpMethod.GET,
                auth(shipperToken), ContractDTO.Response[].class).getBody();
        assertThat(shipperContracts).hasSize(1);
        assertThat(shipperContracts[0].status()).isEqualTo(ContractStatus.ACTIVE);
    }

    @Test
    void shipperCannotCreateContract() {
        createCarrier("FleetMax", "fleet@test.com");
        Long shipperId = createShipper("BuyerCo", "buyer@test.com");
        String shipperToken = loginToken("buyer@test.com", "pass123");

        ResponseEntity<String> response = restTemplate.exchange("/contracts", HttpMethod.POST,
                auth(new ContractDTO.Request(shipperId, LocalDate.of(2026, 1, 1), null), shipperToken),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void terminatedContractCannotBeReactivated() {
        createCarrier("QuickCargo", "quick@test.com");
        String carrierToken = loginToken("quick@test.com", "pass123");
        Long shipperId = createShipper("RetailCo", "retail@test.com");

        Long contractId = restTemplate.exchange("/contracts", HttpMethod.POST,
                auth(new ContractDTO.Request(shipperId, LocalDate.of(2026, 1, 1), null), carrierToken),
                ContractDTO.Response.class).getBody().id();

        restTemplate.exchange("/contracts/" + contractId + "/activate", HttpMethod.PUT, auth(carrierToken), ContractDTO.Response.class);
        restTemplate.exchange("/contracts/" + contractId + "/terminate", HttpMethod.PUT, auth(carrierToken), ContractDTO.Response.class);

        ResponseEntity<String> response = restTemplate.exchange("/contracts/" + contractId + "/activate",
                HttpMethod.PUT, auth(carrierToken), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
