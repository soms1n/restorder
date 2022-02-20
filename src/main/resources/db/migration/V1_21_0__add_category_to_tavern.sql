ALTER TABLE tavern
    ADD COLUMN IF NOT EXISTS category TEXT;

COMMENT ON COLUMN tavern.category IS 'Категория заведения';