CREATE TABLE IF NOT EXISTS blacklist
(
    id           BIGSERIAL NOT NULL
        CONSTRAINT blacklist_pk
            PRIMARY KEY,
    tavern_id    BIGINT    NOT NULL
        CONSTRAINT blacklist_tavern_fk
            REFERENCES tavern (id),
    user_id      BIGINT
        CONSTRAINT blacklist_user_fk
            REFERENCES users (id),
    phone_number TEXT      NOT NULL,
    reason       TEXT      NOT NULL,
    lock_date    TIMESTAMP NOT NULL,
    unlock_date  TIMESTAMP NOT NULL,
    active       BOOLEAN   NOT NULL DEFAULT TRUE
);

COMMENT ON TABLE blacklist IS 'Блокировки пользователей в заведениях';
COMMENT ON COLUMN blacklist.id IS 'Идентификатор записи';
COMMENT ON COLUMN blacklist.tavern_id IS 'Идентификатор заведения';
COMMENT ON COLUMN blacklist.user_id IS 'Идентификатор пользователя';
COMMENT ON COLUMN blacklist.phone_number IS 'Номер заблокированного телефона';
COMMENT ON COLUMN blacklist.reason IS 'Причина';
COMMENT ON COLUMN blacklist.lock_date IS 'Дата блокировки';
COMMENT ON COLUMN blacklist.unlock_date IS 'Дата снятия блокировки';
COMMENT ON COLUMN blacklist.active IS 'Признак активной блокировки';

CREATE UNIQUE INDEX blacklist_tavern_user_uniq ON blacklist (tavern_id, phone_number) WHERE (active IS TRUE);