TRUNCATE TABLE users CASCADE;
TRUNCATE TABLE address CASCADE;
TRUNCATE TABLE event CASCADE;

DELETE FROM contact WHERE user_id = 60;
DELETE FROM user_to_role WHERE user_id = 60;
DELETE FROM tavern_to_employee WHERE user_id = 60;
DELETE FROM users WHERE id = 60;