CREATE TABLE IF NOT EXISTS role
(
    id          BIGSERIAL NOT NULL
        CONSTRAINT role_pk
            PRIMARY KEY,
    code        TEXT      NOT NULL,
    description TEXT      NOT NULL
);

ALTER TABLE role
    ADD CONSTRAINT role_unique UNIQUE (code);

COMMENT ON TABLE role IS 'Роли пользователей';
COMMENT ON COLUMN role.id IS 'Идентификатор записи';
COMMENT ON COLUMN role.code IS 'Код';
COMMENT ON COLUMN role.description IS 'Описание';

INSERT INTO role (code, description)
VALUES ('ADMIN', 'Администратор'),
       ('CLIENT_ADMIN', 'Клиент - администратор'),
       ('CLIENT_EMPLOYEE', 'Клиент - сотрудник'),
       ('USER', 'Пользователь')
ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS user_to_role
(
    id          BIGSERIAL NOT NULL
        CONSTRAINT user_to_role_pk
            PRIMARY KEY,
    user_id     BIGINT    NOT NULL
        CONSTRAINT user_to_role_user_fk
            REFERENCES users (id),
    role        TEXT      NOT NULL
        CONSTRAINT user_to_role_role_fk
            REFERENCES role (code)
);

ALTER TABLE user_to_role
    ADD CONSTRAINT user_to_role_unique UNIQUE (user_id, role);

COMMENT ON TABLE user_to_role IS 'Связь пользователей с ролями';
COMMENT ON COLUMN user_to_role.id IS 'Идентификатор записи';
COMMENT ON COLUMN user_to_role.user_id IS 'Идентификатор пользователя';
COMMENT ON COLUMN user_to_role.role IS 'Роль';