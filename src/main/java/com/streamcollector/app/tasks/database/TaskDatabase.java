package com.streamcollector.app.tasks.database;

import com.streamcollector.app.database.DatabaseUtil;
import com.streamcollector.app.grabber.Platform;
import com.streamcollector.app.settings.Settings;
import com.streamcollector.app.tasks.database.results.TopViewsByUser;
import com.streamcollector.app.tasks.database.results.TopViewsByUserItem;
import org.hibernate.StatelessSession;

import java.time.ZonedDateTime;
import java.util.List;

public class TaskDatabase {
    private StatelessSession session;
    private long sessionTime;

    public List<TopViewsByUserItem> getTopViewsByUser(String username, Platform platform, ZonedDateTime from, ZonedDateTime to, int maxCount){
        var session = updateSession();
        var query = session.createNativeQuery("""
            select row_number() over (order by channel_id) as id, channel_id, name, SUM(GREATEST(t - minus_sec_f - minus_sec_l, 0)) as t_all from
            (
            select channel_id, t, (IF(:per_last < c_l_endTime, TIME_TO_SEC(TIMEDIFF(c_l_endTime,:per_last)), 0)) as minus_sec_l, (IF(:per_first > c_f_startTime, TIME_TO_SEC(TIMEDIFF(:per_first,c_f_startTime)), 0)) as minus_sec_f
            from
            (
            select channel_id, c_f.startTime as 'c_f_startTime', c_l.endTime as 'c_l_endTime', TIME_TO_SEC(TIMEDIFF(c_l.endTime , c_f.startTime)) as t
            from
            `twitch-collector`.users_channels
            join
            `twitch-collector`.circles as c_f on(c_f.id = firstCircle_id)
            join
            `twitch-collector`.circles as c_l on(c_l.id = lastCircle_id)
            where
            user_id = (select id from `twitch-collector`.users where name = :username and site_id = (select id from `twitch-collector`.sites where site = :platform)) and !((c_f.startTime < :per_first and c_l.startTime < :per_first) or (c_f.startTime > :per_last and c_l.startTime > :per_last))
            ) as q
            )
            as w
            join `twitch-collector`.channels on (channels.id = channel_id)
            group by channel_id order by t_all desc
        """, TopViewsByUserItem.class);
        query.setParameter("per_first", from).setParameter("per_last", to).setParameter("username", username).setParameter("platform", platform.getNameInDB());
        query.setMaxResults(maxCount);
        return query.list();
    }

    private StatelessSession updateSession(){
        if (session == null){
            session = DatabaseUtil.getStateLessSession(Settings.instance.getSettings().botDatabase);
            sessionTime = System.currentTimeMillis();
        }else{
            if (System.currentTimeMillis() - sessionTime > 5000){
                session.close();
                session = DatabaseUtil.getStateLessSession(Settings.instance.getSettings().botDatabase);
                sessionTime = System.currentTimeMillis();
            }
        }
        return session;
    }
}
