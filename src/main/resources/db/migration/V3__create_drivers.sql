CREATE TABLE drivers (
    id               BIGSERIAL                   PRIMARY KEY,
    carrier_id       BIGINT                      NOT NULL REFERENCES partners(id),
    name             VARCHAR(255)                NOT NULL,
    email            VARCHAR(255)                NOT NULL UNIQUE,
    password_hash    VARCHAR(255),
    license_number   VARCHAR(100)                NOT NULL UNIQUE,
    license_expiry   DATE                        NOT NULL,
    status           VARCHAR(50)                 NOT NULL DEFAULT 'AVAILABLE',
    deleted_at       TIMESTAMP WITH TIME ZONE,
    created_at       TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT NOW()
);
