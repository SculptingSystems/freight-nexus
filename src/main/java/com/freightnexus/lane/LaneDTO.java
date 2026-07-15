package com.freightnexus.lane;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

public class LaneDTO {

    public record Request(
            @NotBlank String originCity,
            @NotBlank String originCountry,
            @NotNull BigDecimal originLat,
            @NotNull BigDecimal originLon,
            @NotBlank String destinationCity,
            @NotBlank String destinationCountry,
            @NotNull BigDecimal destinationLat,
            @NotNull BigDecimal destinationLon,
            @NotNull @DecimalMin("0.01") BigDecimal distanceKm,
            @NotNull @DecimalMin("0.01") BigDecimal estimatedHours
    ) {}

    public record Response(
            Long id,
            Long carrierId,
            String carrierName,
            String originCity,
            String originCountry,
            String destinationCity,
            String destinationCountry,
            BigDecimal distanceKm,
            BigDecimal estimatedHours,
            String status,
            Instant createdAt
    ) {}
}
