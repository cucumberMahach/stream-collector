package bot;

import database.entities.ChannelToCheckEntity;
import database.entities.TgUserEntity;
import org.hibernate.StatelessSession;

public class BotDatabase {
    protected StatelessSession session;

    public BotDatabase(){
        reloadSession();
    }

    public void reloadSession(){
        session = database.DatabaseUtil.getStateLessSession();
    }

    public TgUserEntity getOrCreateTgUser(TgUserEntity tgUser){
        var query = session.createNativeQuery("select * from `twitch-collector`.tgUsers where tg_id = :tgId", TgUserEntity.class);
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
        session.beginTransaction();
        session.update(tgUser);
        session.getTransaction().commit();
    }
}
