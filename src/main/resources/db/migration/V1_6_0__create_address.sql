CREATE TABLE IF NOT EXISTS address
(
    id        BIGSERIAL NOT NULL
        CONSTRAINT address_pk
            PRIMARY KEY,
    tavern_id BIGINT    NOT NULL
        CONSTRAINT address_tavern_fk
            REFERENCES tavern (id),
    city      TEXT      NOT NULL
        CONSTRAINT address_city_fk
            REFERENCES city (code),
    street    TEXT      NOT NULL,
    building  TEXT      NOT NULL
);

ALTER TABLE address
    ADD CONSTRAINT address_unique UNIQUE (tavern_id, city, street, building);

COMMENT ON TABLE address IS 'Связь пользователей с ролями';
COMMENT ON COLUMN address.id IS 'Идентификатор записи';
COMMENT ON COLUMN address.tavern_id IS 'Идентификатор заведения';
COMMENT ON COLUMN address.city IS 'Город';
COMMENT ON COLUMN address.street IS 'Улица';
COMMENT ON COLUMN address.building IS 'Строение (номер дома)';