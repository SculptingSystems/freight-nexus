CREATE TABLE loads (
    id              BIGSERIAL                   PRIMARY KEY,
    shipment_id     BIGINT                      NOT NULL REFERENCES shipments(id),
    vehicle_id      BIGINT                      NOT NULL REFERENCES vehicles(id),
    driver_id       BIGINT                      NOT NULL REFERENCES drivers(id),
    rate_plan_id    BIGINT                      NOT NULL REFERENCES rate_plans(id),
    pickup_date     DATE                        NOT NULL,
    total_charge    NUMERIC(12,2)               NOT NULL,
    status          VARCHAR(50)                 NOT NULL DEFAULT 'PENDING',
    reference       VARCHAR(50)                 NOT NULL UNIQUE,
    created_at      TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_loads_driver ON loads(driver_id);
CREATE INDEX idx_loads_vehicle ON loads(vehicle_id);
CREATE INDEX idx_loads_shipment ON loads(shipment_id);
