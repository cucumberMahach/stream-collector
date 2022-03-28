package bot;

import database.entities.TgBanEntity;
import database.entities.TgHistoryEntity;
import database.entities.TgUserEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import util.TimeUtil;

public class BotStandardBody extends BotBody {
    private final BotDatabase botDB;

    public BotStandardBody(BotDatabase botDatabase){
        this.botDB = botDatabase;
        initCurrentTime();
    }

    @Override
    public void onUpdate(Update update) {
        initCurrentTime();

        // Уведомления при включении
        var msgTime = TimeUtil.getZonedFromUnix(update.getMessage().getDate());
        if (msgTime.isBefore(bot.getStartTime())){
            var notifications = bot.getStartupNotificationsSet();
            var id = update.getMessage().getFrom().getId().toString();
            if (!notifications.contains(id)){
                notifications.add(id);
                    logic.enableNotificationRequest(update);
            }
            return;
        }

        // Бан
        var tgBan = getActiveBan(update.getMessage().getFrom().getId().toString());
        if (tgBan != null){
            logic.banRequest(update, tgBan);
            return;
        }

        // Загрузка или создание пользователя
        TgUserEntity user = getOrCreateUser(update);
        user.messagesTotal++;

        // Обработка сообщения
        parseMessage(update, user);

        fillUserInfo(update, user);
        botDB.updateTgUser(user);
        addToHistory(update, user, "");
    }

    protected void parseMessage(Update update, TgUserEntity tgUser){
        String message = update.getMessage().getText();

        if (message.equals("/start")){
            logic.startRequest(update, tgUser);
        }else if (message.equals("/help")){
            logic.helpRequest(update, tgUser);
        }else{
            logic.commandRequest(update, tgUser);
        }
    }

    protected void fillUserInfo(Update update, TgUserEntity user){
        user.firstName = update.getMessage().getFrom().getFirstName();
        user.lastName = update.getMessage().getFrom().getLastName();
        user.username = update.getMessage().getFrom().getUserName();
        user.language = update.getMessage().getFrom().getLanguageCode();
        user.lastOnlineTime = currentTime;
    }

    protected TgUserEntity getOrCreateUser(Update update){
        TgUserEntity tgUser = new TgUserEntity();

        tgUser.tgId = update.getMessage().getFrom().getId().toString();
        tgUser.state = "created";
        tgUser.firstOnlineTime = currentTime;
        fillUserInfo(update, tgUser);

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
