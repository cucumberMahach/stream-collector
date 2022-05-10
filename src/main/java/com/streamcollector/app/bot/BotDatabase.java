package com.streamcollector.app.bot;

import com.streamcollector.app.database.DatabaseUtil;
import com.streamcollector.app.database.entities.TgBanEntity;
import com.streamcollector.app.database.entities.TgHistoryEntity;
import com.streamcollector.app.settings.Settings;
import com.streamcollector.app.database.entities.TgUserEntity;
import org.hibernate.StatelessSession;

public class BotDatabase {
    private StatelessSession session;
    private long sessionTime;

    public BotDatabase(){
        var session = updateSession();
    }

    public TgUserEntity getOrCreateTgUser(TgUserEntity tgUser){
        var session = updateSession();
        var query = session.createNativeQuery("select * from `twitch-collector`.tgusers where tg_id = :tgId", TgUserEntity.class);
        query.setParameter("tgId", tgUser.tgId);
        query.setMaxResults(1);
        var user = query.uniqueResult();

        if (user == null){
            session.beginTransaction();
            session.insert(tgUser);
            session.getTransaction().commit();
            return tgUser;
        }else{
            return user;
        }
    }

    public void updateTgUser(TgUserEntity tgUser){
        var session = updateSession();
        session.beginTransaction();
        session.update(tgUser);
        session.getTransaction().commit();
    }

    public TgBanEntity getLastBanOrNull(TgUserEntity tgUser){
        var session = updateSession();
        org.hibernate.query.NativeQuery<TgBanEntity> query;
        if (tgUser.id == null) {
            query = session.createNativeQuery("select * from `twitch-collector`.tgbans where (select id from `twitch-collector`.tgusers where tgusers.tg_id = :tgId) = tgbans.tgUser_id order by tgbans.untilTime desc", TgBanEntity.class);
            query.setParameter("tgId", tgUser.tgId);
        }else{
            query = session.createNativeQuery("select * from `twitch-collector`.tgbans where tgUser_id = :id order by untilTime desc", TgBanEntity.class);
            query.setParameter("id", tgUser.id);
        }
        query.setMaxResults(1);
        var ban = query.uniqueResult();
        return ban;
    }

    public void addToHistory(TgHistoryEntity tgHistory){
        var session = updateSession();
        session.beginTransaction();
        session.insert(tgHistory);
        session.getTransaction().commit();
    }

    public StatelessSession updateSession(){
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
