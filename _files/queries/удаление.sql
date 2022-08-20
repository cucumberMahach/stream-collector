SET @curr = cast('2022-08-20 17:00:00.000' as datetime);
delete
from users_channels
where
TIME_TO_SEC(TIMEDIFF(cast(@curr as datetime), (select endTime from circles where circles.id = users_channels.firstCircle_id))) > 60*60*24*32
and
TIME_TO_SEC(TIMEDIFF(cast(@curr as datetime), (select endTime from circles where circles.id = users_channels.lastCircle_id))) > 60*60*24*32




SET @curr = cast('2022-08-20 17:00:00.000' as datetime);
delete
from channels_circles
where
TIME_TO_SEC(TIMEDIFF(cast(@curr as datetime), collectTime)) > 60*60*24*32



SET @curr = cast('2022-08-20 17:00:00.000' as datetime);
delete
from users
where
TIME_TO_SEC(TIMEDIFF(cast(@curr as datetime), lastVisit)) > 60*60*24*32
AND
NOT EXISTS(select id from users_channels where user_id = users.id);