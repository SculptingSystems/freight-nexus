package com.freightnexus.rateplan;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

public class RatePlanDTO {

    public record Request(
            @NotBlank String name,
            @NotNull PricingModel pricingModel,
            @NotNull @DecimalMin("0.01") BigDecimal baseRate
    ) {}

    public record Response(
            Long id,
            Long laneId,
            String originCity,
            String destinationCity,
            String name,
            PricingModel pricingModel,
            BigDecimal baseRate,
            String status,
            Instant createdAt
    ) {}
}
