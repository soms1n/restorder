CREATE TABLE IF NOT EXISTS event
(
    id              BIGSERIAL NOT NULL
        CONSTRAINT event_pk
            PRIMARY KEY,
    uuid            uuid      NOT NULL,
    type            TEXT      NOT NULL,
    create_date     TIMESTAMP NOT NULL,
    expiration_date TIMESTAMP,
    available       BOOLEAN DEFAULT TRUE,
    params          jsonb
);

COMMENT ON TABLE event IS 'События для обработки';
COMMENT ON COLUMN event.id IS 'Идентификатор записи';
COMMENT ON COLUMN event.uuid IS 'UUID записи для передачи в сообщении';
COMMENT ON COLUMN event.type IS 'Тип';
COMMENT ON COLUMN event.create_date IS 'Дата и время создания';
COMMENT ON COLUMN event.expiration_date IS 'Дата и время истечения события';
COMMENT ON COLUMN event.available IS 'Доступность события';
COMMENT ON COLUMN event.params IS 'Дополнительные параметры';