ALTER TABLE reserve
    ADD COLUMN IF NOT EXISTS manual_mode BOOLEAN DEFAULT FALSE;
ALTER TABLE reserve
    ADD COLUMN IF NOT EXISTS name TEXT;
ALTER TABLE reserve
    ADD COLUMN IF NOT EXISTS phone_number TEXT;

COMMENT ON COLUMN reserve.manual_mode IS 'Забронировано в ручном режиме (оператором)';
COMMENT ON COLUMN reserve.name IS 'Имя (для ручной брони)';
COMMENT ON COLUMN reserve.phone_number IS 'Номер телефона (для ручной брони)';