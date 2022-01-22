CREATE TABLE IF NOT EXISTS schedule
(
    id            BIGSERIAL      NOT NULL
        CONSTRAINT schedule_pk
            PRIMARY KEY,
    tavern_id     BIGINT         NOT NULL
        CONSTRAINT schedule_tavern_fk
            REFERENCES tavern (id),
    day_week      TEXT           NOT NULL,
    time_interval INTERVAL       NOT NULL,
    price         NUMERIC(12, 2) NOT NULL
);

ALTER TABLE schedule
    ADD CONSTRAINT schedule_unique UNIQUE (tavern_id, day_week, time_interval);

COMMENT ON TABLE schedule IS 'График работы';
COMMENT ON COLUMN schedule.id IS 'Идентификатор записи';
COMMENT ON COLUMN schedule.tavern_id IS 'Идентификатор заведения';
COMMENT ON COLUMN schedule.day_week IS 'День недели';
COMMENT ON COLUMN schedule.time_interval IS 'Время работы';
COMMENT ON COLUMN schedule.price IS 'Цена за вход';