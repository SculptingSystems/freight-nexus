package com.freightnexus.rateplan;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lanes")
public class RatePlanController {

    private final RatePlanService service;

    public RatePlanController(RatePlanService service) {
        this.service = service;
    }

    @GetMapping("/{laneId}/rate-plans")
    public List<RatePlanDTO.Response> list(@PathVariable Long laneId) {
        return service.findByLane(laneId);
    }

    @PostMapping("/{laneId}/rate-plans")
    @ResponseStatus(HttpStatus.CREATED)
    public RatePlanDTO.Response create(@PathVariable Long laneId,
                                       @Valid @RequestBody RatePlanDTO.Request request) {
        return service.create(laneId, request);
    }
}
