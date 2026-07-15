package com.freightnexus.contract;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contracts")
public class ContractController {

    private final ContractService service;

    public ContractController(ContractService service) {
        this.service = service;
    }

    @GetMapping
    public List<ContractDTO.Response> list() { return service.findMine(); }

    @GetMapping("/{id}")
    public ContractDTO.Response get(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContractDTO.Response create(@Valid @RequestBody ContractDTO.Request request) {
        return service.create(request);
    }

    @PutMapping("/{id}/activate")
    public ContractDTO.Response activate(@PathVariable Long id) { return service.activate(id); }

    @PutMapping("/{id}/terminate")
    public ContractDTO.Response terminate(@PathVariable Long id) { return service.terminate(id); }
}
