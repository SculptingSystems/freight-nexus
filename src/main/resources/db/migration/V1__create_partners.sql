CREATE TABLE partners (
    id            BIGSERIAL                   PRIMARY KEY,
    name          VARCHAR(255)                NOT NULL,
    email         VARCHAR(255)                NOT NULL UNIQUE,
    type          VARCHAR(50)                 NOT NULL,
    status        VARCHAR(50)                 NOT NULL DEFAULT 'ACTIVE',
    password_hash VARCHAR(255),
    webhook_url   VARCHAR(500),
    deleted_at    TIMESTAMP WITH TIME ZONE,
    created_at    TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT NOW()
);
