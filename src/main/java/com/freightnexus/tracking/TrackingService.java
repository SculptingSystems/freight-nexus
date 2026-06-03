package com.freightnexus.tracking;

import com.freightnexus.auth.FreightPrincipal;
import com.freightnexus.common.ResourceNotFoundException;
import com.freightnexus.load.Load;
import com.freightnexus.load.LoadRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TrackingService {

    private final TrackingRepository trackingRepository;
    private final LoadRepository loadRepository;

    public TrackingService(TrackingRepository trackingRepository, LoadRepository loadRepository) {
        this.trackingRepository = trackingRepository;
        this.loadRepository = loadRepository;
    }

    @Transactional
    public TrackingDTO.EventResponse record(Long loadId, TrackingDTO.EventRequest request) {
        Load load = loadRepository.findByIdWithDetails(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found: " + loadId));

        // Idempotency check — if device retries, return the existing event
        return trackingRepository.findByLoad_IdAndDeviceTimestamp(loadId, request.deviceTimestamp())
                .map(this::toResponse)
                .orElseGet(() -> {
                    TrackingEvent event = new TrackingEvent();
                    event.setLoad(load);
                    event.setLatitude(request.latitude());
                    event.setLongitude(request.longitude());
                    event.setSpeedKmh(request.speedKmh() != null ? request.speedKmh() : BigDecimal.ZERO);
                    event.setHeadingDegrees(request.headingDegrees());
                    event.setDeviceTimestamp(request.deviceTimestamp());
                    return toResponse(trackingRepository.save(event));
                });
    }

    public TrackingDTO.LivePosition getLivePosition(Long loadId) {
        Load load = loadRepository.findByIdWithDetails(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found: " + loadId));

        TrackingEvent latest = trackingRepository
                .findTopByLoad_IdOrderByDeviceTimestampDesc(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("No tracking events for load " + loadId));

        // Moving average speed from last 5 events
        List<TrackingEvent> recent = trackingRepository.findLatestEvents(loadId, 5);
        double avgSpeed = recent.stream()
                .mapToDouble(e -> e.getSpeedKmh().doubleValue())
                .filter(s -> s > 0)
                .average()
                .orElse(60.0); // default to 60 km/h if no valid speed data

        // Haversine distance from current position to destination
        double remaining = HaversineCalculator.distanceKm(
                latest.getLatitude().doubleValue(),
                latest.getLongitude().doubleValue(),
                load.getRatePlan().getLane().getDestinationLat().doubleValue(),
                load.getRatePlan().getLane().getDestinationLon().doubleValue()
        );

        double eta = HaversineCalculator.etaHours(remaining, avgSpeed);

        return new TrackingDTO.LivePosition(
                load.getId(), load.getReference(),
                latest.getLatitude(), latest.getLongitude(), latest.getSpeedKmh(),
                remaining, eta == Double.MAX_VALUE ? null : eta,
                latest.getDeviceTimestamp());
    }

    private TrackingDTO.EventResponse toResponse(TrackingEvent e) {
        return new TrackingDTO.EventResponse(e.getId(), e.getLoad().getId(),
                e.getLatitude(), e.getLongitude(), e.getSpeedKmh(),
                e.getDeviceTimestamp(), e.getRecordedAt());
    }
}
