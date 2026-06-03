-- Immutable audit trail — every status transition is recorded, never deleted or updated.
CREATE TABLE load_status_history (
    id          BIGSERIAL                   PRIMARY KEY,
    load_id     BIGINT                      NOT NULL REFERENCES loads(id),
    from_status VARCHAR(50),
    to_status   VARCHAR(50)                 NOT NULL,
    actor_type  VARCHAR(50)                 NOT NULL,
    actor_id    BIGINT                      NOT NULL,
    note        VARCHAR(500),
    occurred_at TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_lsh_load ON load_status_history(load_id, occurred_at);
