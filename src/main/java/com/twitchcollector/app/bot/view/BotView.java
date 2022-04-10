package com.twitchcollector.app.bot.view;

import com.twitchcollector.app.bot.logic.BotLogic;
import com.twitchcollector.app.database.entities.TgBanEntity;
import com.twitchcollector.app.database.entities.TgUserEntity;
import org.telegram.telegrambots.meta.api.objects.Update;

public abstract class BotView {
    protected BotLogic botLogic;

    public abstract void sendStart(Update update, TgUserEntity tgUser);
    public abstract void sendHelp(Update update, TgUserEntity tgUser);
    public abstract void sendEnableNotification(Update update);
    public abstract void sendBanReply(Update update, TgBanEntity tgBan);

    public void setBotLogic(BotLogic botLogic) {
        this.botLogic = botLogic;
    }
}
