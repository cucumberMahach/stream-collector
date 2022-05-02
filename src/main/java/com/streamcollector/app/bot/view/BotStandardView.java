package com.streamcollector.app.bot.view;

import com.streamcollector.app.bot.commands.UserInfo;
import com.streamcollector.app.util.TimeUtil;
import com.streamcollector.app.database.entities.TgBanEntity;
import com.streamcollector.app.database.entities.TgUserEntity;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.Duration;

public class BotStandardView extends BotView{
    @Override
    public void sendStart(Update update, TgUserEntity tgUser) {
        botLogic.getBotBody().getBot().sendMsg(update.getMessage().getChatId().toString(), """
                &#128075; <b>Вас приветствует Twitch Collector Bot</b>

                Для получении информации о пользователе напишите его &#129313; <b><i>ник</i></b> или отправьте <b><i>ссылку</i></b> на канал &#129300;
                
                &#128591; <b>Пожалуйста</b>, не пробуйте использовать <b>SQL инъекции</b> на нашем боте. Спасибо""");
    }

    @Override
    public void sendHelp(Update update, TgUserEntity tgUser) {
        botLogic.getBotBody().getBot().sendMsg(update.getMessage().getChatId().toString(), """
                &#129488; <b>Помощь</b>
                
                Для получении информации о пользователе напишите его &#129313; <b><i>ник</i></b> или отправьте <b><i>ссылку</i></b> на канал &#129300;
                
                &#128591; <b>Пожалуйста</b>, не пробуйте использовать <b>SQL инъекции</b> на нашем боте. Спасибо""");
    }

    @Override
    public void sendEnableNotification(Update update) {
        botLogic.getBotBody().getBot().sendMsg(update.getMessage().getChatId().toString(), """
                &#128994; Бот снова работает!

                Вы получили это сообщение, потому что пробовали отправить запрос выключенному боту""");
    }

    @Override
    public void sendBanReply(Update update, TgBanEntity tgBan) {
        var banUntil = TimeUtil.formatDurationDays(Duration.between(botLogic.getBotBody().getCurrentTime(), tgBan.untilTime));
        botLogic.getBotBody().getBot().sendMsg(update.getMessage().getChatId().toString(), String.format("""
                &#10060; <b>Вы забанены&#10060;</b>

                &#128221; Причина: <i>%s</i>
                &#128197; До конца бана осталось <b>%s</b>""", tgBan.reason, banUntil));
    }

    @Override
    public void sendUserInfo(Update update, UserInfo userInfo) {
        botLogic.getBotBody().getBot().sendSticker(update.getMessage().getChatId().toString(), "CAACAgIAAxkBAAEERyxiP1p-8qp8alVe51jr5SnwpQxLrgAChhsAAs7QiElhmJB0PqwN7yME", null);
    }
}
