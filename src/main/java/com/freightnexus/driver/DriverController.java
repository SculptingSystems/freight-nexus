package com.freightnexus.driver;

import com.freightnexus.common.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/drivers")
public class DriverController {

    private final DriverService service;

    public DriverController(DriverService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<DriverDTO.Response> list(@PageableDefault(size = 20) Pageable pageable) {
        return service.findMyDrivers(pageable);
    }

    @GetMapping("/{id}")
    public DriverDTO.Response get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DriverDTO.Response create(@Valid @RequestBody DriverDTO.Request request) {
        return service.create(request);
    }
}
