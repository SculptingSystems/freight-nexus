package com.freightnexus.contract;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;

public class ContractDTO {

    public record Request(
            @NotNull Long shipperId,
            @NotNull LocalDate startDate,
            LocalDate endDate
    ) {}

    public record Response(
            Long id,
            Long carrierId,
            String carrierName,
            Long shipperId,
            String shipperName,
            ContractStatus status,
            LocalDate startDate,
            LocalDate endDate,
            Instant createdAt
    ) {}
}
