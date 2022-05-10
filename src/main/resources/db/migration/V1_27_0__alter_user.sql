ALTER TABLE users
    ADD COLUMN IF NOT EXISTS type TEXT;

ALTER TABLE users
    ALTER COLUMN type SET NOT NULL;

COMMENT ON COLUMN users.type IS 'Тип';

CREATE INDEX IF NOT EXISTS users_telegram_id_type_idx ON users (telegram_id, type);