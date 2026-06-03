package com.freightnexus.webhook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WebhookScheduler {

    private static final Logger log = LoggerFactory.getLogger(WebhookScheduler.class);
    private final WebhookOutboxService outboxService;

    public WebhookScheduler(WebhookOutboxService outboxService) {
        this.outboxService = outboxService;
    }

    @Scheduled(fixedDelay = 30_000)
    public void processOutbox() {
        log.debug("Processing webhook outbox");
        outboxService.deliverPending();
    }
}
