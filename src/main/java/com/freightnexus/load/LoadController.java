package com.freightnexus.load;

import com.freightnexus.common.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loads")
public class LoadController {

    private final LoadService service;

    public LoadController(LoadService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<LoadDTO.Response> list(@PageableDefault(size = 20) Pageable pageable) {
        return service.findMine(pageable);
    }

    @GetMapping("/{id}")
    public LoadDTO.Response get(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/{id}/history")
    public List<LoadDTO.HistoryEntry> history(@PathVariable Long id) {
        return service.findHistory(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LoadDTO.Response create(@Valid @RequestBody LoadDTO.Request request) {
        return service.create(request);
    }

    @PutMapping("/{id}/status")
    public LoadDTO.Response updateStatus(@PathVariable Long id,
                                         @Valid @RequestBody LoadDTO.StatusUpdateRequest request) {
        return service.updateStatus(id, request);
    }
}
