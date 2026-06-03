CREATE TABLE rate_plans (
    id             BIGSERIAL                   PRIMARY KEY,
    lane_id        BIGINT                      NOT NULL REFERENCES lanes(id),
    name           VARCHAR(255)                NOT NULL,
    pricing_model  VARCHAR(50)                 NOT NULL,
    base_rate      NUMERIC(12,2)               NOT NULL,
    status         VARCHAR(50)                 NOT NULL DEFAULT 'ACTIVE',
    created_at     TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT NOW()
);
