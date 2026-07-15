package com.freightnexus.hos;

import com.freightnexus.BaseIntegrationTest;
import com.freightnexus.driver.DriverDTO;
import com.freightnexus.hos.HOSService.HOSStatusDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class HOSControllerTest extends BaseIntegrationTest {

    @Test
    void driverHOSStatusStartsAtZero() {
        createCarrier("HOSTestCo", "hos@test.com");
        String carrierToken = loginToken("hos@test.com", "pass123");

        Long driverId = restTemplate.exchange("/drivers", HttpMethod.POST,
                auth(new DriverDTO.Request("Fresh Driver", "fresh@test.com", "dpass",
                        "HOS-LIC-001", LocalDate.of(2028, 6, 1)), carrierToken),
                DriverDTO.Response.class).getBody().id();

        ResponseEntity<HOSStatusDTO> response = restTemplate.exchange(
                "/drivers/" + driverId + "/hos-status", HttpMethod.GET, auth(carrierToken), HOSStatusDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().drivingMinutesToday()).isEqualTo(0);
        assertThat(response.getBody().drivingMinutesRemaining()).isEqualTo(11 * 60);
        assertThat(response.getBody().rolling8DayMinutes()).isEqualTo(0);
        assertThat(response.getBody().rolling8DayRemaining()).isEqualTo(70 * 60);
    }
}
