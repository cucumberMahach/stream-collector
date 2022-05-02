package com.streamcollector.app.bot.view;

import com.streamcollector.app.bot.commands.UserInfo;
import com.streamcollector.app.bot.logic.BotLogic;
import com.streamcollector.app.database.entities.TgBanEntity;
import com.streamcollector.app.database.entities.TgUserEntity;
import org.telegram.telegrambots.meta.api.objects.Update;

public abstract class BotView {
    protected BotLogic botLogic;

    public abstract void sendStart(Update update, TgUserEntity tgUser);
    public abstract void sendHelp(Update update, TgUserEntity tgUser);
    public abstract void sendEnableNotification(Update update);
    public abstract void sendBanReply(Update update, TgBanEntity tgBan);
    public abstract void sendUserInfo(Update update, UserInfo userInfo);

    public void setBotLogic(BotLogic botLogic) {
        this.botLogic = botLogic;
    }
}
