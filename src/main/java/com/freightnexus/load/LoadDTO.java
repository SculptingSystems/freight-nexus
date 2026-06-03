package com.freightnexus.load;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class LoadDTO {

    public record Request(
            @NotNull Long shipmentId,
            @NotNull Long vehicleId,
            @NotNull Long driverId,
            @NotNull Long ratePlanId,
            @NotNull LocalDate pickupDate
    ) {}

    public record Response(
            Long id,
            String reference,
            Long shipmentId,
            String shipperName,
            Long vehicleId,
            String plateNumber,
            Long driverId,
            String driverName,
            Long ratePlanId,
            String laneOrigin,
            String laneDestination,
            LocalDate pickupDate,
            BigDecimal totalCharge,
            LoadStatus status,
            Instant createdAt
    ) {}

    public record StatusUpdateRequest(
            @NotNull LoadStatus status,
            String note
    ) {}

    public record HistoryEntry(
            String fromStatus,
            String toStatus,
            String actorType,
            Long actorId,
            String note,
            Instant occurredAt
    ) {}
}
