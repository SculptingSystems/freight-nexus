package com.freightnexus.lane;

import com.freightnexus.common.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lanes")
public class LaneController {

    private final LaneService service;

    public LaneController(LaneService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<LaneDTO.Response> list(@PageableDefault(size = 20) Pageable pageable) {
        return service.findMyLanes(pageable);
    }

    @GetMapping("/search")
    public List<LaneDTO.Response> search(@RequestParam String origin, @RequestParam String destination) {
        return service.search(origin, destination);
    }

    @GetMapping("/{id}")
    public LaneDTO.Response get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LaneDTO.Response create(@Valid @RequestBody LaneDTO.Request request) {
        return service.create(request);
    }
}
