package bot;

import database.ConfigType;
import database.entities.ChannelToCheckEntity;
import database.entities.TgBanEntity;
import database.entities.TgHistoryEntity;
import database.entities.TgUserEntity;
import org.hibernate.StatelessSession;

import java.time.ZonedDateTime;

public class BotDatabase {
    protected StatelessSession session;

    public BotDatabase(){
        var session = database.DatabaseUtil.getStateLessSession(ConfigType.Local);
        session.close();
    }

    public TgUserEntity getOrCreateTgUser(TgUserEntity tgUser){
        var session = database.DatabaseUtil.getStateLessSession(ConfigType.Local);
        var query = session.createNativeQuery("select * from `twitch-collector`.tgUsers where tg_id = :tgId", TgUserEntity.class);
        query.setParameter("tgId", tgUser.tgId);
        query.setMaxResults(1);
        var user = query.uniqueResult();
        session.close();

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
        var session = database.DatabaseUtil.getStateLessSession(ConfigType.Local);
        session.beginTransaction();
        session.update(tgUser);
        session.getTransaction().commit();
        session.close();
    }

    public TgBanEntity getLastBanOrNull(TgUserEntity tgUser){
        var session = database.DatabaseUtil.getStateLessSession(ConfigType.Local);
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
        session.close();
        return ban;
    }

    public void addToHistory(TgHistoryEntity tgHistory){
        var session = database.DatabaseUtil.getStateLessSession(ConfigType.Local);
        session.beginTransaction();
        session.insert(tgHistory);
        session.getTransaction().commit();
        session.close();
    }
}
