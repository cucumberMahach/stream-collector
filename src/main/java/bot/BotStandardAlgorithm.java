package bot;

import database.entities.TgBanEntity;
import database.entities.TgHistoryEntity;
import database.entities.TgUserEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import util.TimeUtil;

import java.time.Duration;
import java.time.ZonedDateTime;

public class BotStandardAlgorithm implements BotAlgorithm {
    private Bot bot;
    private final BotDatabase botDB;
    private ZonedDateTime currentTime;

    public BotStandardAlgorithm(BotDatabase botDatabase){
        this.botDB = botDatabase;
        initCurrentTime();
    }

    @Override
    public void onUpdate(Update update) {
        initCurrentTime();

        var msgTime = TimeUtil.getZonedFromUnix(update.getMessage().getDate());
        if (msgTime.isBefore(bot.getStartTime())){
            var notifs = bot.getStartupNotificationsSet();
            var id = update.getMessage().getFrom().getId().toString();
            if (!notifs.contains(id)){
                notifs.add(id);
                bot.sendMsg(update.getMessage().getChatId().toString(), "&#128994; Бот включен");
            }
            return;
        }

        var ban = getActiveBan(update.getMessage().getFrom().getId().toString());
        if (ban != null){
            var banUntil = TimeUtil.formatDurationDays(Duration.between(currentTime, ban.untilTime));
            bot.sendMsg(update.getMessage().getChatId().toString(), String.format("<b>&#10060;Вы забанены&#10060;</b>\n\n&#128221; Причина: <i>%s</i>.\n&#128197; До конца бана осталось <b>%s</b>", ban.reason, banUntil));
            return;
        }
        TgUserEntity user = getOrCreateUser(update);
        user.messagesTotal++;


        bot.sendSticker(update.getMessage().getChatId().toString(), "CAACAgIAAxkBAAEERyxiP1p-8qp8alVe51jr5SnwpQxLrgAChhsAAs7QiElhmJB0PqwN7yME", null);


        fillUserInfo(update, user);
        botDB.updateTgUser(user);
        addToHistory(update, user, "");
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

    protected void initCurrentTime(){
        currentTime = TimeUtil.getZonedNow();
    }

    @Override
    public void setBot(Bot bot) {
        this.bot = bot;
    }
}
