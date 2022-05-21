ALTER TABLE tavern
    ADD COLUMN IF NOT EXISTS link_table_layout TEXT;

COMMENT ON COLUMN tavern.link_table_layout IS 'Ссылка на схему столов';