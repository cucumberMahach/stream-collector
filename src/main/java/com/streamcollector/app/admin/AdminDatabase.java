package com.streamcollector.app.admin;

import com.streamcollector.app.admin.stages.dataViews.TgBanView;
import com.streamcollector.app.admin.stages.dataViews.TgHistoryView;
import com.streamcollector.app.database.DatabaseUtil;
import com.streamcollector.app.database.SharedDatabase;
import com.streamcollector.app.settings.Settings;
import com.streamcollector.app.admin.stages.dataViews.TgUserView;
import com.streamcollector.app.database.entities.TgBanEntity;
import com.streamcollector.app.database.entities.TgHistoryEntity;
import com.streamcollector.app.database.entities.TgUserEntity;
import com.streamcollector.app.util.StringUtils;
import org.hibernate.StatelessSession;

import java.time.ZonedDateTime;
import java.util.ArrayList;

public class AdminDatabase {
    public AdminDatabase(){
        var session = getSession();
        session.close();
    }

    public ArrayList<TgHistoryView> searchHistory(TgUserEntity tgUser, String text, Integer count, ZonedDateTime date){
        var history = new ArrayList<TgHistoryView>();

        var session = getSession();

        text = StringUtils.formatUserQuery(text);

        String q = "select * from `twitch-collector`.tghistory where (id like :text or tgUser_id like :text or message like :text or result like :text)";
        if (tgUser != null)
            q += " and tgUser_id = :userId";
        if (date != null)
            q += " and (date(messageTime) = date(:date) or date(requestTime) = date(:date) or date(answerTime) = date(:date))";
        q += " order by messageTime desc";

        var query = session.createNativeQuery(q, TgHistoryEntity.class);
        query.setParameter("text", "%" + text + "%");
        if (tgUser != null)
            query.setParameter("userId", tgUser.id);
        if (date != null)
            query.setParameter("date", date);
        if (count != null)
            query.setMaxResults(count);

        var result = query.list();
        result.sort((o1, o2) -> o1.messageTime.toInstant().compareTo(o2.messageTime.toInstant()));
        for (var hist : result){
            history.add(TgHistoryView.fromTgHistory(hist));
        }

        return history;
    }

    public ArrayList<TgUserView> searchUsers(String text, Integer count){
        var users = new ArrayList<TgUserView>();

        var session = getSession();
        var query = session.createNativeQuery("select * from `twitch-collector`.tgusers where id like :text or tg_id like :text or firstName like :text or lastName like :text or username like :text or language like :text or messagesTotal like :text or state like :text", TgUserEntity.class);
        query.setParameter("text", "%" + text + "%");
        if (count != null)
            query.setMaxResults(count);

        var result = query.list();

        for (var user : result){
            users.add(TgUserView.fromTgUser(user));
        }

        return users;
    }

    public ArrayList<TgBanView> getBans(TgUserEntity tgUser, ZonedDateTime currentTime){
        var bans = new ArrayList<TgBanView>();

        var session = getSession();

        var result = SharedDatabase.getBans(session, tgUser, currentTime);

        for (var ban : result){
            bans.add(TgBanView.fromTgBan(ban, currentTime));
        }

        return bans;
    }

    public boolean makeBan(TgBanEntity tgBan){
        var session = getSession();
        return SharedDatabase.makeBan(session, tgBan);
    }

    public boolean stopBan(TgUserEntity tgUser, ZonedDateTime currentTime){
        var session = getSession();
        return SharedDatabase.stopBan(session, tgUser, currentTime);
    }

    public void updateTgBan(TgBanEntity tgBan){
        var session = getSession();
        session.beginTransaction();
        session.update(tgBan);
        session.getTransaction().commit();
    }

    private StatelessSession getSession(){
        var settings = Settings.instance.getSettings();
        return DatabaseUtil.getStateLessSession(settings.adminDatabase);
    }
}
