CREATE TABLE IF NOT EXISTS users
(
    id          BIGSERIAL NOT NULL
        CONSTRAINT users_pk
            PRIMARY KEY,
    last_name   TEXT,
    first_name  TEXT,
    middle_name TEXT NOT NULL,
    telegram_id BIGSERIAL NOT NULL,
    blocked     BOOLEAN DEFAULT FALSE
);

ALTER TABLE users
    ADD CONSTRAINT users_telegram_id_unique UNIQUE (telegram_id);

COMMENT ON TABLE users IS 'Пользователи системы';
COMMENT ON COLUMN users.id IS 'Идентификатор записи';
COMMENT ON COLUMN users.last_name IS 'Фамилия';
COMMENT ON COLUMN users.first_name IS 'Имя';
COMMENT ON COLUMN users.middle_name IS 'Отчество';
COMMENT ON COLUMN users.telegram_id IS 'Идентификатор в telegram';
COMMENT ON COLUMN users.blocked IS 'Признак блокировки';