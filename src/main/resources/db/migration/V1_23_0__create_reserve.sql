CREATE TABLE IF NOT EXISTS reserve
(
    id            BIGSERIAL NOT NULL
        CONSTRAINT schedule_pk
            PRIMARY KEY,
    user_id       BIGINT    NOT NULL
        CONSTRAINT reserve_user_fk
            REFERENCES users (id),
    table_id      BIGINT    NOT NULL
        CONSTRAINT reserve_table_fk
            REFERENCES tables (id),
    number_people INT       NOT NULL,
    status        TEXT      NOT NULL,
    date          DATE      NOT NULL,
    time          TIMESTAMP NOT NULL
);

COMMENT ON TABLE reserve IS 'Брони столов';
COMMENT ON COLUMN reserve.id IS 'Идентификатор записи';
COMMENT ON COLUMN reserve.user_id IS 'Идентификатор пользователя';
COMMENT ON COLUMN reserve.table_id IS 'Идентификатор стола';
COMMENT ON COLUMN reserve.number_people IS 'Кол-во персон';
COMMENT ON COLUMN reserve.status IS 'Статус';
COMMENT ON COLUMN reserve.date IS 'Дата';
COMMENT ON COLUMN reserve.time IS 'Время';