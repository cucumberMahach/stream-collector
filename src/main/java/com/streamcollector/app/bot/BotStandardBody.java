package com.streamcollector.app.bot;

import com.streamcollector.app.bot.logic.BotStandardLogic;
import com.streamcollector.app.database.entities.TgBanEntity;
import com.streamcollector.app.database.entities.TgHistoryEntity;
import com.streamcollector.app.util.TimeUtil;
import com.streamcollector.app.database.entities.TgUserEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Random;

public class BotStandardBody{
    protected ZonedDateTime currentTime;
    protected Bot bot;
    protected BotStandardLogic logic;

    private final BotDatabase botDB;
    private final Random random = new Random();

    public BotStandardBody(BotDatabase botDatabase){
        this.botDB = botDatabase;
        initCurrentTime();
    }

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
        }else if (message.equals("/account")) {
            logic.accountRequest(update, tgUser);
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
        tgUser.donationKey = createDonationKey(tgApiUser.getId());

        fillUserInfo(tgApiUser, tgUser);

        tgUser = botDB.getOrCreateTgUser(tgUser);
        return tgUser;
    }

    private String createDonationKey(Long tgId){
        String originalKey = String.format("%d%d%d", Math.abs(random.nextInt(Short.MAX_VALUE)), tgId, Math.abs(random.nextInt(Short.MAX_VALUE)));
        return Base64.getEncoder().encodeToString(originalKey.getBytes(StandardCharsets.UTF_8));
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

    protected void initCurrentTime(){
        currentTime = TimeUtil.getZonedNow();
    }

    public void setBot(Bot bot) {
        this.bot = bot;
    }

    public Bot getBot() {
        return bot;
    }

    public ZonedDateTime getCurrentTime() {
        return currentTime;
    }

    public BotStandardLogic getLogic() {
        return logic;
    }

    public void setLogic(BotStandardLogic logic) {
        this.logic = logic;
        logic.setBotBody(this);
    }
}
