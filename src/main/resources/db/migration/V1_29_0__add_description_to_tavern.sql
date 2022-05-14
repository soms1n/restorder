ALTER TABLE tavern
    ADD COLUMN IF NOT EXISTS description TEXT;

COMMENT ON COLUMN tavern.description IS 'Описание';