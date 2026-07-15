package com.freightnexus.tracking;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/loads")
public class TrackingController {

    private final TrackingService service;

    public TrackingController(TrackingService service) {
        this.service = service;
    }

    // Driver posts GPS ping, idempotent by (load_id, device_timestamp)
    @PostMapping("/{id}/tracking")
    @ResponseStatus(HttpStatus.CREATED)
    public TrackingDTO.EventResponse recordEvent(@PathVariable Long id,
                                                 @Valid @RequestBody TrackingDTO.EventRequest request) {
        return service.record(id, request);
    }

    // Live position + Haversine ETA, available to shipper, carrier, and driver
    @GetMapping("/{id}/tracking/live")
    public TrackingDTO.LivePosition livePosition(@PathVariable Long id) {
        return service.getLivePosition(id);
    }
}
