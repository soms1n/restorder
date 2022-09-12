ALTER TABLE tavern
    ADD COLUMN IF NOT EXISTS valid BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN tavern.valid IS 'Признак валидного заведения';