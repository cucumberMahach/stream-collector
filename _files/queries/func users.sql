DROP FUNCTION IF EXISTS manageUsers;

CREATE FUNCTION manageUsers(
	p_name VARCHAR(45),
	p_site TINYINT UNSIGNED,
    p_visit DATETIME(3)
)
RETURNS TINYINT
BEGIN
    DECLARE v_res TINYINT;
    DECLARE v_user_id BIGINT UNSIGNED;
    SET v_res = 0;
	SET v_user_id = 0;
	select id into v_user_id from users where users.name = p_name and users.site_id = p_site;
	IF (v_user_id > 0) THEN
		UPDATE users SET users.lastVisit = p_visit WHERE users.id = v_user_id;
		SET v_res = 1;
	ELSE
		INSERT INTO users (name,site_id,lastVisit) VALUES (p_name,p_site,p_visit);
		SET v_res = 2;
	END IF;
	RETURN v_res;
END;
