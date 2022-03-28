package bot.logic;

import database.entities.TgBanEntity;
import database.entities.TgUserEntity;
import org.telegram.telegrambots.meta.api.objects.Update;

public class BotStandardLogic extends BotLogic{
    @Override
    public void startRequest(Update update, TgUserEntity tgUser) {
        view.sendStart(update, tgUser);
    }

    @Override
    public void helpRequest(Update update, TgUserEntity tgUser) {
        view.sendHelp(update, tgUser);
    }

    @Override
    public void enableNotificationRequest(Update update) {
        view.sendEnableNotification(update);
    }

    @Override
    public void banRequest(Update update, TgBanEntity tgBan) {
        view.sendBanReply(update, tgBan);
    }

    @Override
    public void commandRequest(Update update, TgUserEntity tgUser) {
        body.getBot().sendSticker(update.getMessage().getChatId().toString(), "CAACAgIAAxkBAAEERyxiP1p-8qp8alVe51jr5SnwpQxLrgAChhsAAs7QiElhmJB0PqwN7yME", null);
    }
}
