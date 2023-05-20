CREATE TABLE IF NOT EXISTS blacklist_setting
(
    id        BIGSERIAL NOT NULL
        CONSTRAINT blacklist_setting_pk
            PRIMARY KEY,
    tavern_id BIGINT    NOT NULL
        CONSTRAINT blacklist_setting_tavern_fk
            REFERENCES tavern (id),
    times     INT       NOT NULL DEFAULT 0,
    days      INT       NOT NULL
);

COMMENT ON TABLE blacklist_setting IS 'Настройки блокировок пользователей в заведениях';
COMMENT ON COLUMN blacklist_setting.id IS 'Идентификатор записи';
COMMENT ON COLUMN blacklist_setting.tavern_id IS 'Идентификатор заведения';
COMMENT ON COLUMN blacklist_setting.times IS 'Кол-во раз, когда человек не пришел в заведение (0 отключить)';
COMMENT ON COLUMN blacklist_setting.days IS 'Кол-во дней блокировки (0 навсегда)';

CREATE UNIQUE INDEX blacklist_setting_tavern_id_uniq ON blacklist_setting (tavern_id);