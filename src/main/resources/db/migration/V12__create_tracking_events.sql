-- Append-only GPS events from driver devices.
-- Unique on (load_id, device_timestamp), idempotent, safe to retry from device.
CREATE TABLE tracking_events (
    id               BIGSERIAL                   PRIMARY KEY,
    load_id          BIGINT                      NOT NULL REFERENCES loads(id),
    latitude         NUMERIC(9,6)                NOT NULL,
    longitude        NUMERIC(9,6)                NOT NULL,
    speed_kmh        NUMERIC(6,2)                NOT NULL DEFAULT 0,
    heading_degrees  NUMERIC(5,2),
    device_timestamp TIMESTAMP WITH TIME ZONE    NOT NULL,
    recorded_at      TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_tracking_load_time UNIQUE (load_id, device_timestamp)
);

CREATE INDEX idx_tracking_load_time ON tracking_events(load_id, device_timestamp DESC);
