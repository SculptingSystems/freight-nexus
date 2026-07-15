package com.freightnexus.partner;

import com.freightnexus.common.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/partners")
public class PartnerController {

    private final PartnerService service;

    public PartnerController(PartnerService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<PartnerDTO.Response> list(@PageableDefault(size = 20) Pageable pageable) {
        return service.findAll(pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PartnerDTO.Response create(@Valid @RequestBody PartnerDTO.Request request) {
        return service.create(request);
    }

    @PutMapping("/{id}/webhook")
    public PartnerDTO.Response registerWebhook(@PathVariable Long id,
                                               @RequestBody PartnerDTO.WebhookRequest request) {
        return service.registerWebhook(id, request.url());
    }
}
