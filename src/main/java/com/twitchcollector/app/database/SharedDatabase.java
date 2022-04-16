package com.twitchcollector.app.database;

import com.twitchcollector.app.database.entities.TgBanEntity;
import com.twitchcollector.app.database.entities.TgUserEntity;
import org.hibernate.StatelessSession;

import java.time.ZonedDateTime;
import java.util.ArrayList;

public class SharedDatabase {
    public static ArrayList<TgBanEntity> getBans(StatelessSession session, TgUserEntity tgUser, ZonedDateTime currentTime){
        var bans = new ArrayList<TgBanEntity>();

        var query = session.createNativeQuery("select * from `twitch-collector`.tgbans where tgUser_id = :id order by fromTime,untilTime", TgBanEntity.class);
        query.setParameter("id", tgUser.id);

        var result = query.list();

        return bans;
    }

    public static boolean makeBan(StatelessSession session, TgBanEntity tgBan){
        var query = session.createNativeQuery("select * from `twitch-collector`.tgbans where tgUser_id = :id order by untilTime desc", TgBanEntity.class);
        query.setParameter("id", tgBan.tgUser.id);
        query.setMaxResults(1);
        var ban = query.uniqueResult();
        if (ban != null){
            if (ban.untilTime.isBefore(tgBan.fromTime)){
                session.beginTransaction();
                session.insert(tgBan);
                session.getTransaction().commit();
                return true;
            }else{
                return false;
            }
        }else{
            session.beginTransaction();
            session.insert(tgBan);
            session.getTransaction().commit();
            return true;
        }
    }

    public static boolean stopBan(StatelessSession session, TgUserEntity tgUser, ZonedDateTime currentTime){
        var query = session.createNativeQuery("select * from `twitch-collector`.tgbans where tgUser_id = :id order by untilTime desc", TgBanEntity.class);
        query.setParameter("id", tgUser.id);
        query.setMaxResults(1);
        var ban = query.uniqueResult();
        if (ban != null && ban.untilTime.isAfter(currentTime)){
            ban.untilTime = currentTime;
            session.beginTransaction();
            session.update(ban);
            session.getTransaction().commit();
            return true;
        }
        return false;
    }
}
