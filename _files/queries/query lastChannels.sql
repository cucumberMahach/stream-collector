только каналы + даты:
select name as channelName, sub2.time, type from
(select sub.channel_id, sub.type_id, MAX(sub.collectTime) as time from
(select users_channels.channel_id as channel_id, type_id, collectTime from
users_channels
join circles on (users_channels.lastCircle_id = circles.id)
join channels_circles on (channels_circles.circle_id = users_channels.lastCircle_id and channels_circles.channel_id = users_channels.channel_id)
where user_id = (select users.id from users where name = 'corel_qqq' and site_id = 1)) as sub
group by sub.channel_id
limit 10) as sub2
join user_types on (user_types.id = sub2.type_id)
join channels on (channels.id = sub2.channel_id)
order by sub2.time desc;


часть по времени:
select users_channels.channel_id, SUM(TIMESTAMPDIFF(SECOND, cf.startTime, cl.endTime)) as sec, MAX(ccl.collectTime) as time
from users_channels
join circles as cf on (users_channels.firstCircle_id = cf.id)
join circles as cl on (users_channels.lastCircle_id = cl.id)
join channels_circles as ccf on (ccf.circle_id = users_channels.firstCircle_id and ccf.channel_id = users_channels.channel_id)
join channels_circles as ccl on (ccl.circle_id = users_channels.lastCircle_id and ccl.channel_id = users_channels.channel_id)
where user_id = (select users.id from users where name = 'cyclic171' and site_id = (select id from sites where site = 'twitch'))
group by users_channels.channel_id
order by time desc
limit 10


готовый:
select name as channelName, sub_sec.time, type, sub_sec.sec from
(select users_channels.channel_id, SUM(TIMESTAMPDIFF(SECOND, cf.startTime, cl.endTime)) as sec, MAX(ccl.collectTime) as time, MIN(type_id) as type_id
from users_channels
join circles as cf on (users_channels.firstCircle_id = cf.id)
join circles as cl on (users_channels.lastCircle_id = cl.id)
join channels_circles as ccl on (ccl.circle_id = users_channels.lastCircle_id and ccl.channel_id = users_channels.channel_id)
where user_id = (select users.id from users where name = 'corel_qqq' and site_id = (select id from sites where site = 'twitch'))
group by users_channels.channel_id
order by time desc
limit 10) as sub_sec
join user_types on (user_types.id = sub_sec.type_id)
join channels on (channels.id = sub_sec.channel_id)
order by sub_sec.time desc;