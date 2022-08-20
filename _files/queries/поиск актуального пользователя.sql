select user_name, q.id, lastVisit, site, ch_id, channels.name as ch_name from
(
select users.name as user_name, users.id as id, MAX(endTime) as lastVisit, sites.site as site, users_channels.channel_id as ch_id from `twitch-collector`.users_channels join `twitch-collector`.users on(users_channels.user_id = users.id) join `twitch-collector`.circles on(circles.id = users_channels.lastCircle_id) join `twitch-collector`.sites on(sites.id = users.site_id) where users.name like '%\_\_\_\_\_%' group by users.id order by MAX(endTime) desc
) as q
join
channels on(channels.id = ch_id);