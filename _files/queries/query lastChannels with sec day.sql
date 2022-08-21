
create table temp
                  (
                      channel_id bigint unsigned  null,
                      time       datetime(3)      null,
                      type_id    tinyint unsigned null
                  )

delete from temp where channel_id != 0;

insert into temp (channel_id, time, type_id) select users_channels.channel_id, MAX(ccl.collectTime) as time, MIN(type_id) as type_id
from users_channels
join channels_circles as ccl on (ccl.circle_id = users_channels.lastCircle_id and ccl.channel_id = users_channels.channel_id)
where user_id = (select users.id from users where name = 'corel_qqq' and site_id = (select id from sites where site = 'twitch'))
group by users_channels.channel_id
order by time desc
limit 10;

select name as channelName, sub_sec.time, type, sub_sec.sec from
(select users_channels.channel_id, SUM(TIMESTAMPDIFF(SECOND, cf.startTime, cl.endTime)) as sec, MAX(ccl.collectTime) as time, MIN(type_id) as type_id
from users_channels
join circles as cf on (users_channels.firstCircle_id = cf.id)
join circles as cl on (users_channels.lastCircle_id = cl.id)
join channels_circles as ccf on (ccf.circle_id = users_channels.firstCircle_id and ccf.channel_id = users_channels.channel_id)
join channels_circles as ccl on (ccl.circle_id = users_channels.lastCircle_id and ccl.channel_id = users_channels.channel_id)
where user_id = (select users.id from users where name = 'corel_qqq' and site_id = (select id from sites where site = 'twitch'))
 and DATE(ccl.collectTime) = (select DATE(v.time) from temp as v where v.channel_id = users_channels.channel_id and v.type_id = users_channels.type_id)
group by users_channels.channel_id
order by time desc
limit 10) as sub_sec
join user_types on (user_types.id = sub_sec.type_id)
join channels on (channels.id = sub_sec.channel_id)
order by sub_sec.time desc;