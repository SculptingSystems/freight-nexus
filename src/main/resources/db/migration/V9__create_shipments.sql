CREATE TABLE shipments (
    id                  BIGSERIAL                   PRIMARY KEY,
    shipper_id          BIGINT                      NOT NULL REFERENCES partners(id),
    description         VARCHAR(500)                NOT NULL,
    weight_kg           NUMERIC(10,2)               NOT NULL,
    volume_m3           NUMERIC(10,2)               NOT NULL,
    hazmat              BOOLEAN                     NOT NULL DEFAULT FALSE,
    declared_value_usd  NUMERIC(12,2),
    status              VARCHAR(50)                 NOT NULL DEFAULT 'PENDING',
    created_at          TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT NOW()
);
