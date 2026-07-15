package com.freightnexus.capacity;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CapacityDTO {

    public record Request(
            @NotNull LocalDate capacityDate,
            @NotNull @DecimalMin("0.01") BigDecimal totalWeightKg,
            @NotNull @DecimalMin("0.01") BigDecimal totalVolumeM3
    ) {}

    public record Response(
            Long id,
            Long vehicleId,
            String plateNumber,
            LocalDate capacityDate,
            BigDecimal totalWeightKg,
            BigDecimal bookedWeightKg,
            BigDecimal availableWeightKg,
            BigDecimal totalVolumeM3,
            BigDecimal bookedVolumeM3,
            BigDecimal availableVolumeM3
    ) {}
}
