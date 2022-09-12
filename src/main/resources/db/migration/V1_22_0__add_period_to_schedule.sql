ALTER TABLE schedule
    ADD COLUMN IF NOT EXISTS start_period TIME;
ALTER TABLE schedule
    ADD COLUMN IF NOT EXISTS end_period TIME;

COMMENT ON COLUMN schedule.start_period IS 'Начало работы';
COMMENT ON COLUMN schedule.end_period IS 'Окончание работы';

ALTER TABLE schedule DROP COLUMN IF EXISTS time_interval;

ALTER TABLE schedule ALTER COLUMN price TYPE INT;