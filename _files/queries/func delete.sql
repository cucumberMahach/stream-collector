DROP FUNCTION IF EXISTS removeUnnecessary;

CREATE FUNCTION removeUnnecessary(
    p_curr DATETIME(3)
)
RETURNS TINYINT
BEGIN
	DECLARE v_res TINYINT;
	DECLARE v_interval INT;
	SET v_res = 0;
	SET v_interval = 60*60*24*32;
	
	delete
	from users_channels
	where
	TIMESTAMPDIFF(SECOND, (select endTime from circles where circles.id = users_channels.firstCircle_id), p_curr) > v_interval
	and
	TIMESTAMPDIFF(SECOND, (select endTime from circles where circles.id = users_channels.lastCircle_id), p_curr) > v_interval;

	delete
	from channels_circles
	where
	TIMESTAMPDIFF(SECOND, collectTime, p_curr) > v_interval;

	delete
	from users
	where
	TIMESTAMPDIFF(SECOND, lastVisit, p_curr) > v_interval
	AND
	NOT EXISTS(select id from users_channels where user_id = users.id);
	
	RETURN v_res;
END;
