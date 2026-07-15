package com.freightnexus.partner;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public class PartnerDTO {

    public record Request(
            @NotBlank String name,
            @NotBlank @Email String email,
            @NotNull PartnerType type,
            @NotBlank String password
    ) {}

    public record Response(
            Long id,
            String name,
            String email,
            PartnerType type,
            PartnerStatus status,
            Instant createdAt
    ) {}

    public record WebhookRequest(String url) {}
}
