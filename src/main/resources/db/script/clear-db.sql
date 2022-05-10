TRUNCATE TABLE users CASCADE;
TRUNCATE TABLE address CASCADE;
TRUNCATE TABLE event CASCADE;

DO
$$
    DECLARE
        v_user_id   BIGINT := 57;
        v_tavern_id BIGINT;
    BEGIN
        DELETE FROM contact WHERE user_id = v_user_id;
        DELETE FROM user_to_role WHERE user_id = v_user_id;

        SELECT tavern_id INTO v_tavern_id FROM tavern_to_employee WHERE user_id = v_user_id;
        DELETE FROM tavern_to_employee WHERE user_id = v_user_id;
        DELETE FROM address WHERE id = (SELECT t.address_id FROM tavern t WHERE t.id = v_tavern_id);
        DELETE FROM tavern WHERE id = v_tavern_id;
        DELETE FROM reserve WHERE user_id = v_user_id;
        DELETE FROM users WHERE id = v_user_id;
    END
$$ LANGUAGE plpgsql;