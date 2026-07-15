CREATE TABLE lanes (
    id                  BIGSERIAL                   PRIMARY KEY,
    carrier_id          BIGINT                      NOT NULL REFERENCES partners(id),
    origin_city         VARCHAR(100)                NOT NULL,
    origin_country      VARCHAR(100)                NOT NULL,
    origin_lat          NUMERIC(9,6)                NOT NULL,
    origin_lon          NUMERIC(9,6)                NOT NULL,
    destination_city    VARCHAR(100)                NOT NULL,
    destination_country VARCHAR(100)                NOT NULL,
    destination_lat     NUMERIC(9,6)                NOT NULL,
    destination_lon     NUMERIC(9,6)                NOT NULL,
    distance_km         NUMERIC(10,2)               NOT NULL,
    estimated_hours     NUMERIC(6,2)                NOT NULL,
    status              VARCHAR(50)                 NOT NULL DEFAULT 'ACTIVE',
    created_at          TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT NOW()
);
