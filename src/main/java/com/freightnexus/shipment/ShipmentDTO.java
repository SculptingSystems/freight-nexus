package com.freightnexus.shipment;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

public class ShipmentDTO {

    public record Request(
            @NotBlank String description,
            @NotNull @DecimalMin("0.01") BigDecimal weightKg,
            @NotNull @DecimalMin("0.01") BigDecimal volumeM3,
            boolean hazmat,
            BigDecimal declaredValueUsd
    ) {}

    public record Response(
            Long id,
            Long shipperId,
            String shipperName,
            String description,
            BigDecimal weightKg,
            BigDecimal volumeM3,
            boolean hazmat,
            BigDecimal declaredValueUsd,
            ShipmentStatus status,
            Instant createdAt
    ) {}
}
