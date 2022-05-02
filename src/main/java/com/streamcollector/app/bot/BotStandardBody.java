package com.streamcollector.app.bot;

import com.streamcollector.app.database.entities.TgBanEntity;
import com.streamcollector.app.database.entities.TgHistoryEntity;
import com.streamcollector.app.util.TimeUtil;
import com.streamcollector.app.database.entities.TgUserEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

public class BotStandardBody extends BotBody {
    private final BotDatabase botDB;

    public BotStandardBody(BotDatabase botDatabase){
        this.botDB = botDatabase;
        initCurrentTime();
    }

    @Override
    public void onUpdate(Update update) {
        User tgApiUser;
        String chatId;
        if (update.getMessage() == null){
            if (update.getCallbackQuery() == null){
                return;
            }else{
                chatId = update.getCallbackQuery().getMessage().getChatId().toString();
                tgApiUser = update.getCallbackQuery().getFrom();
            }
        }else{
            chatId = update.getMessage().getChatId().toString();
            tgApiUser = update.getMessage().getFrom();
        }


        initCurrentTime();

        // Уведомления при включении
        if (update.getMessage() != null) {
            var msgTime = TimeUtil.getZonedFromUnix(update.getMessage().getDate());
            if (msgTime.isBefore(bot.getStartTime())) {
                var notifications = bot.getStartupNotificationsSet();
                var id = update.getMessage().getFrom().getId().toString();
                if (!notifications.contains(id)) {
                    notifications.add(id);
                    logic.enableNotificationRequest(update);
                }
                return;
            }
        }

        // Бан
        var tgBan = getActiveBan(tgApiUser.getId().toString());
        if (tgBan != null){
            logic.banRequest(chatId, tgBan);
            return;
        }

        // Загрузка или создание пользователя
        TgUserEntity user = getOrCreateUser(tgApiUser);
        user.messagesTotal++;

        // Обработка сообщения
        parseMessage(update, user);

        fillUserInfo(tgApiUser, user);
        botDB.updateTgUser(user);
        if (update.getMessage() != null)
            addToHistory(update, user, "");
    }

    @Override
    public BotDatabase getDatabase() {
        return botDB;
    }

    protected void parseMessage(Update update, TgUserEntity tgUser){
        if (update.getMessage() == null){
            if (update.getCallbackQuery() != null){
                logic.callbackRequest(update, tgUser);
            }
            return;
        }

        String message = update.getMessage().getText();

        if (message.equals("/start")){
            logic.startRequest(update, tgUser);
        }else if (message.equals("/help")){
            logic.helpRequest(update, tgUser);
        }else{
            logic.commandRequest(update, tgUser);
        }
    }

    protected void fillUserInfo(User tgApiUser, TgUserEntity user){
        user.firstName = tgApiUser.getFirstName();
        user.lastName = tgApiUser.getLastName();
        user.username = tgApiUser.getUserName();
        user.language = tgApiUser.getLanguageCode();
        user.lastOnlineTime = currentTime;
    }

    protected TgUserEntity getOrCreateUser(User tgApiUser){
        TgUserEntity tgUser = new TgUserEntity();

        tgUser.tgId = tgApiUser.getId().toString();
        tgUser.state = "created";
        tgUser.firstOnlineTime = currentTime;
        fillUserInfo(tgApiUser, tgUser);

        tgUser = botDB.getOrCreateTgUser(tgUser);
        return tgUser;
    }

    protected TgBanEntity getActiveBan(String tgId){
        TgUserEntity user = new TgUserEntity();
        user.tgId = tgId;
        var ban = botDB.getLastBanOrNull(user);
        if (ban == null)
            return null;
        if (ban.untilTime.isAfter(currentTime))
            return ban;
        return null;
    }

    protected void addToHistory(Update update, TgUserEntity user, String result){
        TgHistoryEntity tgHistory = new TgHistoryEntity();
        tgHistory.tgUser = user;
        var msg = update.getMessage().getText().strip();
        tgHistory.message = msg.length() > 300 ? msg.substring(0, 300) : msg;
        tgHistory.result = result;
        tgHistory.messageTime = TimeUtil.getZonedFromUnix(update.getMessage().getDate());
        tgHistory.requestTime = currentTime;
        tgHistory.answerTime = TimeUtil.getZonedNow();
        botDB.addToHistory(tgHistory);
    }
}
