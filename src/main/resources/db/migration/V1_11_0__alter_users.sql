ALTER TABLE users
    ADD COLUMN IF NOT EXISTS state TEXT;
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS sub_state TEXT;

COMMENT ON COLUMN users.state IS 'Состояние';
COMMENT ON COLUMN users.sub_state IS 'Подсостояние';

ALTER TABLE users ALTER COLUMN middle_name DROP NOT NULL;