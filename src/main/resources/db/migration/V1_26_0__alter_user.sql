alter table users add column IF NOT EXISTS registered BOOLEAN NOT NULL DEFAULT FALSE;
COMMENT ON COLUMN users.registered IS 'Признак зарегистрированного пользователя';