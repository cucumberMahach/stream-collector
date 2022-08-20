package com.streamcollector.app.tasks.database;

import com.streamcollector.app.database.DatabaseUtil;
import com.streamcollector.app.database.entities.UserEntity;
import com.streamcollector.app.grabber.Platform;
import com.streamcollector.app.settings.Settings;
import com.streamcollector.app.tasks.database.results.TopViewsByUserItem;
import com.streamcollector.app.tasks.database.results.UserSearchItem;
import com.streamcollector.app.util.StringUtils;
import org.hibernate.StatelessSession;

import java.time.ZonedDateTime;
import java.util.List;

public class TaskDatabase {
    private StatelessSession session;
    private long sessionTime;

    public List<UserSearchItem> searchUsers(String text, int maxCount, boolean concreteUser){
        var session = updateSession();
        text = StringUtils.formatUserQuery(text);
        var query = session.createNativeQuery("""
        select users.id as id, name as user_name, lastVisit, site
        from users
        join sites on (sites.id = users.site_id)
        where name like :text
        order by lastVisit desc
        """, UserSearchItem.class);
        query.setParameter("text", concreteUser ? text : "%" + text + "%");
        query.setMaxResults(maxCount);
        return query.list();
    }

    public UserEntity getUser(String username, Platform platform){
        var session = updateSession();
        var query = session.createNativeQuery("select * from `twitch-collector`.users where name = :username and site_id = (select id from `twitch-collector`.sites where site = :platform)", UserEntity.class);
        query.setParameter("username", username);
        query.setParameter("platform", platform.getNameInDB());
        query.setMaxResults(1);
        return query.uniqueResult();
    }

    public List<TopViewsByUserItem> getTopViewsByUser(String username, Platform platform, ZonedDateTime from, ZonedDateTime to, int maxCount){
        var session = updateSession();
        var query = session.createNativeQuery("""
            select row_number() over (order by channel_id) as id, channel_id, name, SUM(GREATEST(t - minus_sec_f - minus_sec_l, 0)) as t_all from
            (
            select channel_id, t, (IF(:per_last < c_l_endTime, TIMESTAMPDIFF(SECOND,:per_last, c_l_endTime), 0)) as minus_sec_l, (IF(:per_first > c_f_startTime, TIMESTAMPDIFF(SECOND,c_f_startTime,:per_first), 0)) as minus_sec_f
            from
            (
            select channel_id, c_f.startTime as 'c_f_startTime', c_l.endTime as 'c_l_endTime', TIMESTAMPDIFF(SECOND, c_f.startTime,c_l.endTime ) as t
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
