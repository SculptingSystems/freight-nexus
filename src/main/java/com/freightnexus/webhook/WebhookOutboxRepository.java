package com.freightnexus.webhook;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface WebhookOutboxRepository extends JpaRepository<WebhookOutbox, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT w FROM WebhookOutbox w JOIN FETCH w.partner
            WHERE w.deliveredAt IS NULL AND w.retryCount < 10 AND w.nextRetryAt <= :now
            ORDER BY w.nextRetryAt
            """)
    List<WebhookOutbox> findPendingForDelivery(@Param("now") Instant now);
}
