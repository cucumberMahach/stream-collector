package com.streamcollector.app.bot.logic;

import com.streamcollector.app.bot.view.BotView;
import com.streamcollector.app.bot.BotBody;
import com.streamcollector.app.database.entities.TgBanEntity;
import com.streamcollector.app.database.entities.TgUserEntity;
import org.telegram.telegrambots.meta.api.objects.Update;

public abstract class BotLogic {
    protected BotBody body;
    protected BotView view;

    public abstract void startRequest(Update update, TgUserEntity tgUser);
    public abstract void helpRequest(Update update, TgUserEntity tgUser);
    public abstract void enableNotificationRequest(Update update);
    public abstract void banRequest(String chatId, TgBanEntity tgBan);
    public abstract void commandRequest(Update update, TgUserEntity tgUser);
    public abstract void callbackRequest(Update update, TgUserEntity tgUser);

    public void setBotBody(BotBody body) {
        this.body = body;
    }

    public BotBody getBotBody() {
        return body;
    }

    public BotView getBotView() {
        return view;
    }

    public void setBotView(BotView view) {
        this.view = view;
        view.setBotLogic(this);
    }
}
