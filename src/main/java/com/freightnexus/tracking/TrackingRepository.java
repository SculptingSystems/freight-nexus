package com.freightnexus.tracking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TrackingRepository extends JpaRepository<TrackingEvent, Long> {

    Optional<TrackingEvent> findTopByLoad_IdOrderByDeviceTimestampDesc(Long loadId);

    Optional<TrackingEvent> findByLoad_IdAndDeviceTimestamp(Long loadId, java.time.Instant deviceTimestamp);

    // Last N events for moving-average speed calculation
    @Query(value = "SELECT * FROM tracking_events WHERE load_id = :loadId ORDER BY device_timestamp DESC LIMIT :limit",
           nativeQuery = true)
    List<TrackingEvent> findLatestEvents(@Param("loadId") Long loadId, @Param("limit") int limit);
}
