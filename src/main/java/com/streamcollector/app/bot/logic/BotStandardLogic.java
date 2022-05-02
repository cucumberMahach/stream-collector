package com.streamcollector.app.bot.logic;

import com.streamcollector.app.bot.commands.UserInfo;
import com.streamcollector.app.database.entities.TgBanEntity;
import com.streamcollector.app.database.entities.TgUserEntity;
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
        var userInfo = showUserInfo(update.getMessage().getText());
        view.sendUserInfo(update, userInfo);
    }

    private UserInfo showUserInfo(String userName){
        var userInfo = new UserInfo();

        return userInfo;
    }
}
