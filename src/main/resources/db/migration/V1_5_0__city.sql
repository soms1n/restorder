CREATE TABLE IF NOT EXISTS city
(
    id          BIGSERIAL NOT NULL
        CONSTRAINT city_pk
            PRIMARY KEY,
    code        TEXT      NOT NULL,
    description TEXT      NOT NULL
);

ALTER TABLE city
    ADD CONSTRAINT city_unique UNIQUE (code);

COMMENT ON TABLE city IS 'Справочник городов';
COMMENT ON COLUMN city.id IS 'Идентификатор записи';
COMMENT ON COLUMN city.code IS 'Код';
COMMENT ON COLUMN city.description IS 'Описание';

INSERT INTO city (code, description)
VALUES ('BRYANSK', 'Брянск'),
       ('YOSHKAR_OLA', 'Йошкар-Ола')
ON CONFLICT DO NOTHING;