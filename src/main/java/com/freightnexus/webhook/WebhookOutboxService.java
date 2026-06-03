package com.freightnexus.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightnexus.partner.Partner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Service
public class WebhookOutboxService {

    private static final Logger log = LoggerFactory.getLogger(WebhookOutboxService.class);

    private final WebhookOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public WebhookOutboxService(WebhookOutboxRepository outboxRepository,
                                ObjectMapper objectMapper,
                                @Qualifier("webhookRestTemplate") RestTemplate restTemplate) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    @Transactional
    public void enqueue(Partner partner, String eventType, Object payload) {
        WebhookOutbox entry = new WebhookOutbox();
        entry.setPartner(partner);
        entry.setEventType(eventType);
        entry.setNextRetryAt(Instant.now());
        try {
            entry.setPayload(objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize webhook payload", e);
        }
        outboxRepository.save(entry);
    }

    @Transactional
    public void deliverPending() {
        outboxRepository.findPendingForDelivery(Instant.now()).forEach(entry -> {
            String url = entry.getPartner().getWebhookUrl();
            if (url == null || url.isBlank()) {
                entry.setDeliveredAt(Instant.now());
                outboxRepository.save(entry);
                return;
            }
            try {
                restTemplate.postForEntity(url, entry.getPayload(), Void.class);
                entry.setDeliveredAt(Instant.now());
                log.info("Webhook delivered: event={} url={}", entry.getEventType(), url);
            } catch (Exception e) {
                int attempts = entry.getRetryCount() + 1;
                entry.setRetryCount(attempts);
                long backoffSeconds = (long) Math.pow(2, attempts) * 60;
                entry.setNextRetryAt(Instant.now().plusSeconds(backoffSeconds));
                log.warn("Webhook delivery failed (attempt {}/10): {}", attempts, e.getMessage());
            }
            outboxRepository.save(entry);
        });
    }
}
