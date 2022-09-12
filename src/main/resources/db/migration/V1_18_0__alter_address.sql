ALTER TABLE address DROP COLUMN IF EXISTS tavern_id;
ALTER TABLE address DROP COLUMN IF EXISTS building;

COMMENT ON TABLE address IS 'Адреса заведений';