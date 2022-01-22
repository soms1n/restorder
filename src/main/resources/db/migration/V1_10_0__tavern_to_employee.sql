CREATE TABLE IF NOT EXISTS tavern_to_employee
(
    id           BIGSERIAL NOT NULL
        CONSTRAINT tavern_to_employee_pk
            PRIMARY KEY,
    tavern_id    BIGINT    NOT NULL
        CONSTRAINT tavern_to_employee_tavern_fk
            REFERENCES tavern (id),
    user_id    BIGINT    NOT NULL
        CONSTRAINT tavern_to_employee_user_fk
            REFERENCES users (id)
);


COMMENT ON TABLE tavern_to_employee IS 'Столы в заведении';
COMMENT ON COLUMN tavern_to_employee.id IS 'Идентификатор записи';
COMMENT ON COLUMN tavern_to_employee.tavern_id IS 'Идентификатор заведения';
COMMENT ON COLUMN tavern_to_employee.user_id IS 'Идентификатор сотрудника';