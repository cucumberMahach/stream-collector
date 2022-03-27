package bot;

import database.entities.TgUserEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import util.TimeUtil;

public class BotStandardAlgorithm implements BotAlgorithm {
    private Bot bot;
    private final BotDatabase botDB;

    public BotStandardAlgorithm(BotDatabase botDatabase){
        this.botDB = botDatabase;
    }

    @Override
    public void onUpdate(Update update) {
        TgUserEntity user = getOrCreateUser(update);
        if (user.banned)
            return;
        user.messagesTotal++;


        bot.sendSticker(update.getMessage().getChatId().toString(), "CAACAgIAAxkBAAEERyxiP1p-8qp8alVe51jr5SnwpQxLrgAChhsAAs7QiElhmJB0PqwN7yME", null);


        fillUserInfo(update, user);
        botDB.updateTgUser(user);
    }

    protected void fillUserInfo(Update update, TgUserEntity user){
        user.firstName = update.getMessage().getFrom().getFirstName();
        user.lastName = update.getMessage().getFrom().getLastName();
        user.username = update.getMessage().getFrom().getUserName();
        user.language = update.getMessage().getFrom().getLanguageCode();
        user.lastOnlineTime = TimeUtil.getZonedNow();
    }

    protected TgUserEntity getOrCreateUser(Update update){
        TgUserEntity tgUser = new TgUserEntity();

        tgUser.tgId = update.getMessage().getFrom().getId().toString();
        tgUser.state = "created";
        fillUserInfo(update, tgUser);

        tgUser = botDB.getOrCreateTgUser(tgUser);
        return tgUser;
    }

    @Override
    public void setBot(Bot bot) {
        this.bot = bot;
    }
}
