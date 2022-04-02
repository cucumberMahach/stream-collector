package admin;

import admin.stages.bans.BanLine;
import admin.stages.bans.BanUser;
import database.ConfigType;
import database.entities.TgBanEntity;
import database.entities.TgUserEntity;
import org.hibernate.StatelessSession;

import java.time.ZonedDateTime;
import java.util.ArrayList;

public class AdminDatabase {
    public AdminDatabase(){
        var session = getSession();
        session.close();
    }

    public ArrayList<BanUser> searchBanUsers(String text, Integer count){
        var users = new ArrayList<BanUser>();

        var session = getSession();
        var query = session.createNativeQuery("select * from `twitch-collector`.tgusers where id like :text or tg_id like :text or firstName like :text or lastName like :text or username like :text or language like :text or messagesTotal like :text or state like :text or firstOnlineTime like :text or lastOnlineTime like :text", TgUserEntity.class);
        query.setParameter("text", "%" + text + "%");
        if (count != null)
            query.setMaxResults(count);

        var result = query.list();

        for (var user : result){
            users.add(BanUser.fromTgUserEntity(user));
        }

        return users;
    }

    public ArrayList<BanLine> getBans(TgUserEntity tgUser, ZonedDateTime currentTime){
        var bans = new ArrayList<BanLine>();

        var session = getSession();
        var query = session.createNativeQuery("select * from `twitch-collector`.tgbans where tgUser_id = :id order by fromTime,untilTime", TgBanEntity.class);
        query.setParameter("id", tgUser.id);

        var result = query.list();

        for (var ban : result){
            bans.add(BanLine.fromTgBan(ban, currentTime));
        }

        return bans;
    }

    public boolean makeBan(TgBanEntity tgBan){
        var session = getSession();

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

    public boolean stopBan(TgUserEntity tgUser, ZonedDateTime currentTime){
        var session = getSession();

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

    public void updateTgBan(TgBanEntity tgBan){
        var session = getSession();
        session.beginTransaction();
        session.update(tgBan);
        session.getTransaction().commit();
    }

    private StatelessSession getSession(){
        return database.DatabaseUtil.getStateLessSession(ConfigType.Local);
    }
}
