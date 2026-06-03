-- HOS (Hours of Service) window tracks FMCSA regulatory state per driver per day.
-- Rules enforced: 11h driving/day, 14h on-duty window, 30min break after 8h, 70h/8-day rolling.
CREATE TABLE hos_windows (
    id                           BIGSERIAL   PRIMARY KEY,
    driver_id                    BIGINT      NOT NULL REFERENCES drivers(id),
    window_date                  DATE        NOT NULL,
    driving_minutes              INT         NOT NULL DEFAULT 0,
    on_duty_minutes              INT         NOT NULL DEFAULT 0,
    consecutive_driving_minutes  INT         NOT NULL DEFAULT 0,
    on_duty_start_at             TIMESTAMP WITH TIME ZONE,
    last_break_at                TIMESTAMP WITH TIME ZONE,
    created_at                   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at                   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_hos_driver_date UNIQUE (driver_id, window_date)
);

CREATE INDEX idx_hos_driver_date ON hos_windows(driver_id, window_date DESC);
