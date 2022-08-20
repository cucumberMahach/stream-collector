DROP FUNCTION IF EXISTS manageUsersChannels;

CREATE FUNCTION manageUsersChannels(
	p_ch_id BIGINT UNSIGNED,
	p_cur_cir_id BIGINT UNSIGNED,
	p_pre_cir_id BIGINT UNSIGNED,
	p_name VARCHAR(45),
	p_type TINYINT UNSIGNED,
	p_site TINYINT UNSIGNED,
    p_fits_by_prev TINYINT,
    p_datetime DATETIME(3)
)
RETURNS TINYINT
BEGIN
    DECLARE v_col_time DATETIME(3);
    DECLARE v_old_usr_ch_id BIGINT UNSIGNED;
    DECLARE v_res TINYINT;
    DECLARE v_user_id BIGINT UNSIGNED;
    SET v_col_time = '2000-01-01 00:00:00';
    SET v_old_usr_ch_id = 0;
    SET v_res = 0;
    IF (p_pre_cir_id > 0) THEN
        select collectTime, q.id as usr_ch_id into v_col_time, v_old_usr_ch_id from (select * from `twitch-collector`.users_channels where user_id = (select id from `twitch-collector`.users where name = p_name and site_id = p_site) and channel_id = p_ch_id and type_id = p_type) as q join `twitch-collector`.circles on (q.lastCircle_id = circles.id) join channels_circles on(channels_circles.circle_id = q.lastCircle_id and channels_circles.channel_id = q.channel_id) where endTime is not null order by endTime desc limit 1;
        IF ((p_fits_by_prev and v_old_usr_ch_id != 0) or (v_old_usr_ch_id != 0 and p_datetime < v_col_time)) THEN
            UPDATE users_channels SET users_channels.lastCircle_id = p_cur_cir_id WHERE users_channels.id = v_old_usr_ch_id;
            SET v_res = 1;
        END IF;
    END IF;
    IF (v_res = 0) THEN
        select q.id into v_user_id from `twitch-collector`.users as q where name = p_name and site_id = p_site;
        INSERT INTO users_channels (user_id, channel_id, firstCircle_id, lastCircle_id, type_id) VALUES (v_user_id, p_ch_id, p_cur_cir_id, p_cur_cir_id, p_type);
        SET v_res = 2;
    END IF;
	RETURN v_res;
END;
