package com.twitchcollector.app.bot.view;

import com.twitchcollector.app.database.entities.TgBanEntity;
import com.twitchcollector.app.database.entities.TgUserEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import com.twitchcollector.app.util.TimeUtil;

import java.time.Duration;

public class BotStandardView extends BotView{
    @Override
    public void sendStart(Update update, TgUserEntity tgUser) {

    }

    @Override
    public void sendHelp(Update update, TgUserEntity tgUser) {

    }

    @Override
    public void sendEnableNotification(Update update) {
        botLogic.getBotBody().getBot().sendMsg(update.getMessage().getChatId().toString(), "&#128994; Бот снова работает!\n\nВы получили это сообщение, потому что пробовали отправить запрос выключенному боту");
    }

    @Override
    public void sendBanReply(Update update, TgBanEntity tgBan) {
        var banUntil = TimeUtil.formatDurationDays(Duration.between(botLogic.getBotBody().getCurrentTime(), tgBan.untilTime));
        botLogic.getBotBody().getBot().sendMsg(update.getMessage().getChatId().toString(), String.format("<b>&#10060;Вы забанены&#10060;</b>\n\n&#128221; Причина: <i>%s</i>\n&#128197; До конца бана осталось <b>%s</b>", tgBan.reason, banUntil));
    }
}
