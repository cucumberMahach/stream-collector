SET @per_f = cast('2022-05-09 20:00:00.000' as datetime);
SET @per_l = cast('2022-05-09 23:40:00.000' as datetime);

select row_number() over (order by channel_id) as id, channel_id, name, SUM(GREATEST(t - minus_sec_f - minus_sec_l, 0)) as t_all from
(
select channel_id, t, (IF(cast(@per_l as datetime) < c_l_endTime, TIMESTAMPDIFF(SECOND,cast(@per_l as datetime),c_l_endTime), 0)) as minus_sec_l, (IF(cast(@per_f as datetime) > c_f_startTime, TIMESTAMPDIFF(SECOND,c_f_startTime,cast(@per_f as datetime)), 0)) as minus_sec_f
from
(
select channel_id, c_l.startTime as 'c_l_startTime', c_f.startTime as 'c_f_startTime', c_l.endTime as 'c_l_endTime', c_f.endTime as 'c_f_endTime', TIMESTAMPDIFF(SECOND, c_f.startTime,c_l.endTime) as t
from
users_channels
join
circles as c_f on(c_f.id = firstCircle_id)
join
circles as c_l on(c_l.id = lastCircle_id)
where
user_id = (select id from users where name = 'corel_qqq' and site_id = (select id from sites where site = 'twitch')) and !((c_f.startTime < cast(@per_f as datetime) and c_l.startTime < cast(@per_f as datetime)) or (c_f.startTime > cast(@per_l as datetime) and c_l.startTime > cast(@per_l as datetime)))
) as q
)
as w
join channels on (channels.id = channel_id)
group by channel_id order by t_all desc