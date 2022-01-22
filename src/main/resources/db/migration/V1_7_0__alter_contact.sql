ALTER TABLE contact
    ADD COLUMN IF NOT EXISTS tavern_id BIGINT;

ALTER TABLE IF EXISTS contact
    ADD CONSTRAINT contact_tavern_fk
        FOREIGN KEY (tavern_id) REFERENCES tavern (id);

COMMENT ON COLUMN contact.tavern_id IS 'Владелец (заведение)';