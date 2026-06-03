-- Tracks available weight/volume per vehicle per date for capacity booking.
CREATE TABLE vehicle_capacity (
    id                      BIGSERIAL   PRIMARY KEY,
    vehicle_id              BIGINT      NOT NULL REFERENCES vehicles(id),
    capacity_date           DATE        NOT NULL,
    total_weight_kg         NUMERIC(10,2) NOT NULL,
    booked_weight_kg        NUMERIC(10,2) NOT NULL DEFAULT 0,
    total_volume_m3         NUMERIC(10,2) NOT NULL,
    booked_volume_m3        NUMERIC(10,2) NOT NULL DEFAULT 0,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_vehicle_capacity_date UNIQUE (vehicle_id, capacity_date)
);
