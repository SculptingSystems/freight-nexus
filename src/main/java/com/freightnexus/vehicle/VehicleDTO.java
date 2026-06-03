package com.freightnexus.vehicle;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

public class VehicleDTO {

    public record Request(
            @NotBlank String plateNumber,
            @NotNull VehicleType type,
            @NotNull @DecimalMin("0.01") BigDecimal weightCapacityKg,
            @NotNull @DecimalMin("0.01") BigDecimal volumeCapacityM3
    ) {}

    public record Response(
            Long id,
            Long carrierId,
            String carrierName,
            String plateNumber,
            VehicleType type,
            BigDecimal weightCapacityKg,
            BigDecimal volumeCapacityM3,
            VehicleStatus status,
            Instant createdAt
    ) {}
}
