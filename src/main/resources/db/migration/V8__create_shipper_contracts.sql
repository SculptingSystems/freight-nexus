CREATE TABLE shipper_contracts (
    id              BIGSERIAL                   PRIMARY KEY,
    carrier_id      BIGINT                      NOT NULL REFERENCES partners(id),
    shipper_id      BIGINT                      NOT NULL REFERENCES partners(id),
    status          VARCHAR(50)                 NOT NULL DEFAULT 'DRAFT',
    start_date      DATE                        NOT NULL,
    end_date        DATE,
    created_at      TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_contract_pair UNIQUE (carrier_id, shipper_id)
);
