CREATE TABLE IF NOT EXISTS visiting
(
    id           BIGSERIAL NOT NULL
        CONSTRAINT visiting_pk
            PRIMARY KEY,
    user_id      BIGINT    NOT NULL
        CONSTRAINT visiting_user_fk
            REFERENCES users (id),
    tavern_id    BIGINT    NOT NULL
        CONSTRAINT visiting_tavern_fk
            REFERENCES tavern (id),
    phone_number TEXT      NOT NULL,
    times        INT       NOT NULL DEFAULT 0
);

COMMENT ON TABLE visiting IS 'Учёт посещений клиентов в заведениях (для автоматической блокировки)';
COMMENT ON COLUMN visiting.user_id IS 'Идентификатор пользователя';
COMMENT ON COLUMN visiting.tavern_id IS 'Идентификатор заведения';
COMMENT ON COLUMN visiting.phone_number IS 'Номер телефона';
COMMENT ON COLUMN visiting.times IS 'Кол-во раз, когда человек не пришел в заведение';

CREATE UNIQUE INDEX visiting_user_id_tavern_id_uniq ON visiting (user_id, phone_number, tavern_id);