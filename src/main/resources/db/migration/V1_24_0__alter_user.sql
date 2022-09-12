alter table users add column IF NOT EXISTS city text;
COMMENT ON COLUMN users.city IS 'Город для поиска заведений';