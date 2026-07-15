package com.freightnexus.partner;

import com.freightnexus.auth.FreightPrincipal;
import com.freightnexus.common.ForbiddenException;
import com.freightnexus.common.PageResponse;
import com.freightnexus.common.ResourceNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PartnerService {

    private final PartnerRepository repository;
    private final PasswordEncoder passwordEncoder;

    public PartnerService(PartnerRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public PageResponse<PartnerDTO.Response> findAll(Pageable pageable) {
        return PageResponse.from(repository.findAll(pageable).map(this::toResponse));
    }

    @Transactional
    public PartnerDTO.Response create(PartnerDTO.Request request) {
        Partner partner = new Partner();
        partner.setName(request.name());
        partner.setEmail(request.email());
        partner.setType(request.type());
        partner.setPasswordHash(passwordEncoder.encode(request.password()));
        return toResponse(repository.save(partner));
    }

    @Transactional
    public PartnerDTO.Response registerWebhook(Long id, String url) {
        FreightPrincipal caller = caller();
        if (!caller.id().equals(id)) {
            throw new ForbiddenException("You can only register webhooks for your own account");
        }
        Partner partner = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found: " + id));
        partner.setWebhookUrl(url);
        return toResponse(repository.save(partner));
    }

    private FreightPrincipal caller() {
        return (FreightPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private PartnerDTO.Response toResponse(Partner p) {
        return new PartnerDTO.Response(p.getId(), p.getName(), p.getEmail(),
                p.getType(), p.getStatus(), p.getCreatedAt());
    }
}
