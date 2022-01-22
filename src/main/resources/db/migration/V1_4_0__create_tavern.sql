CREATE TABLE IF NOT EXISTS tavern
(
    id        BIGSERIAL NOT NULL
        CONSTRAINT tavern_pk
            PRIMARY KEY,
    user_id BIGINT    NOT NULL
        CONSTRAINT tavern_user_fk
            REFERENCES users (id),
    name      TEXT      NOT NULL
);

COMMENT ON TABLE tavern IS 'Заведения';
COMMENT ON COLUMN tavern.id IS 'Идентификатор записи';
COMMENT ON COLUMN tavern.user_id IS 'Идентификатор владельца';
COMMENT ON COLUMN tavern.name IS 'Название';