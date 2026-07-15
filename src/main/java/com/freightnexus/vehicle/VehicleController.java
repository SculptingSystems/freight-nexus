package com.freightnexus.vehicle;

import com.freightnexus.common.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleService service;

    public VehicleController(VehicleService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<VehicleDTO.Response> list(@PageableDefault(size = 20) Pageable pageable) {
        return service.findMyVehicles(pageable);
    }

    @GetMapping("/{id}")
    public VehicleDTO.Response get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleDTO.Response create(@Valid @RequestBody VehicleDTO.Request request) {
        return service.create(request);
    }
}
