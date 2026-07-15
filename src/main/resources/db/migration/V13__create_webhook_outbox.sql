CREATE TABLE webhook_outbox (
    id            BIGSERIAL                   PRIMARY KEY,
    partner_id    BIGINT                      NOT NULL REFERENCES partners(id),
    event_type    VARCHAR(100)                NOT NULL,
    payload       TEXT                        NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT NOW(),
    delivered_at  TIMESTAMP WITH TIME ZONE,
    retry_count   INT                         NOT NULL DEFAULT 0,
    next_retry_at TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_webhook_outbox_pending ON webhook_outbox(next_retry_at)
    WHERE delivered_at IS NULL;
