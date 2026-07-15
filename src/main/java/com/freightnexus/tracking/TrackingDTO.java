package com.freightnexus.tracking;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

public class TrackingDTO {

    public record EventRequest(
            @NotNull BigDecimal latitude,
            @NotNull BigDecimal longitude,
            BigDecimal speedKmh,
            BigDecimal headingDegrees,
            @NotNull Instant deviceTimestamp
    ) {}

    public record EventResponse(
            Long id,
            Long loadId,
            BigDecimal latitude,
            BigDecimal longitude,
            BigDecimal speedKmh,
            Instant deviceTimestamp,
            Instant recordedAt
    ) {}

    public record LivePosition(
            Long loadId,
            String reference,
            BigDecimal currentLat,
            BigDecimal currentLon,
            BigDecimal speedKmh,
            Double distanceRemainingKm,
            Double etaHours,
            Instant lastUpdated
    ) {}
}
