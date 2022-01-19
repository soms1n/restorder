CREATE TABLE IF NOT EXISTS contact
(
    id          BIGSERIAL NOT NULL
        CONSTRAINT contact_pk
            PRIMARY KEY,
    user_id     BIGINT    NOT NULL
        CONSTRAINT contact_user_fk
            REFERENCES users (id),
    type        TEXT      NOT NULL,
    value       TEXT      NOT NULL,
    main        BOOLEAN DEFAULT FALSE,
    active      BOOLEAN DEFAULT FALSE,
    create_date TIMESTAMP NOT NULL
);

ALTER TABLE contact
    ADD CONSTRAINT contact_user_type_value_unique UNIQUE (user_id, type, value);

COMMENT ON TABLE contact IS 'Контакты пользователей';
COMMENT ON COLUMN contact.id IS 'Идентификатор записи';
COMMENT ON COLUMN contact.user_id IS 'Идентификатор владельца';
COMMENT ON COLUMN contact.type IS 'Тип';
COMMENT ON COLUMN contact.value IS 'Значение';
COMMENT ON COLUMN contact.main IS 'Признак главного';
COMMENT ON COLUMN contact.active IS 'Признак действующего';
COMMENT ON COLUMN contact.create_date IS 'Дата и время создания';