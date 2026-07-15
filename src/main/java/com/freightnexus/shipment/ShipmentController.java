package com.freightnexus.shipment;

import com.freightnexus.common.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shipments")
public class ShipmentController {

    private final ShipmentService service;

    public ShipmentController(ShipmentService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<ShipmentDTO.Response> list(@PageableDefault(size = 20) Pageable pageable) {
        return service.findMine(pageable);
    }

    @GetMapping("/{id}")
    public ShipmentDTO.Response get(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShipmentDTO.Response create(@Valid @RequestBody ShipmentDTO.Request request) {
        return service.create(request);
    }
}
