ALTER TABLE tavern
    ADD COLUMN IF NOT EXISTS address_id BIGINT;

ALTER TABLE IF EXISTS tavern
    ADD CONSTRAINT tavern_address_fk
        FOREIGN KEY (address_id) REFERENCES address (id);

COMMENT ON COLUMN tavern.address_id IS 'Адрес';