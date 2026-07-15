package com.freightnexus.capacity;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/vehicles")
public class CapacityController {

    private final CapacityService service;

    public CapacityController(CapacityService service) {
        this.service = service;
    }

    @PutMapping("/{id}/capacity")
    public CapacityDTO.Response setCapacity(@PathVariable Long id,
                                            @Valid @RequestBody CapacityDTO.Request request) {
        return service.setCapacity(id, request);
    }

    @GetMapping("/{id}/capacity")
    public CapacityDTO.Response getCapacity(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.getCapacity(id, date);
    }
}
