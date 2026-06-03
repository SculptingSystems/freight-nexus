package com.freightnexus.driver;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;

public class DriverDTO {

    public record Request(
            @NotBlank String name,
            @NotBlank @Email String email,
            @NotBlank String password,
            @NotBlank String licenseNumber,
            @NotNull LocalDate licenseExpiry
    ) {}

    public record Response(
            Long id,
            Long carrierId,
            String carrierName,
            String name,
            String email,
            String licenseNumber,
            LocalDate licenseExpiry,
            DriverStatus status,
            Instant createdAt
    ) {}
}
