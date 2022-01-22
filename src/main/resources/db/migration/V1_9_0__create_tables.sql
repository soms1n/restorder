CREATE TABLE IF NOT EXISTS tables
(
    id           BIGSERIAL NOT NULL
        CONSTRAINT tables_pk
            PRIMARY KEY,
    tavern_id    BIGINT    NOT NULL
        CONSTRAINT tables_tavern_fk
            REFERENCES tavern (id),
    number_seats INTEGER   NOT NULL,
    reserved     BOOLEAN   NOT NULL,
    label        TEXT
);


COMMENT ON TABLE tables IS 'Столы в заведении';
COMMENT ON COLUMN tables.id IS 'Идентификатор записи';
COMMENT ON COLUMN tables.tavern_id IS 'Идентификатор заведения';
COMMENT ON COLUMN tables.number_seats IS 'Кол-во мест';
COMMENT ON COLUMN tables.reserved IS 'Признак зарезервированного';
COMMENT ON COLUMN tables.label IS 'Метка';