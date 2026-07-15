CREATE TABLE vehicles (
    id                    BIGSERIAL                   PRIMARY KEY,
    carrier_id            BIGINT                      NOT NULL REFERENCES partners(id),
    plate_number          VARCHAR(50)                 NOT NULL UNIQUE,
    type                  VARCHAR(50)                 NOT NULL,
    weight_capacity_kg    NUMERIC(10,2)               NOT NULL,
    volume_capacity_m3    NUMERIC(10,2)               NOT NULL,
    status                VARCHAR(50)                 NOT NULL DEFAULT 'ACTIVE',
    deleted_at            TIMESTAMP WITH TIME ZONE,
    created_at            TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT NOW()
);
