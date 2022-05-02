package com.streamcollector.app.bot.logic;

import com.streamcollector.app.bot.commands.ButtonWithData;
import com.streamcollector.app.bot.commands.LoadingMessage;
import com.streamcollector.app.bot.commands.UserInfo;
import com.streamcollector.app.bot.commands.UserPlatformChoice;
import com.streamcollector.app.database.entities.TgBanEntity;
import com.streamcollector.app.database.entities.TgUserEntity;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

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
    public void banRequest(String chatId, TgBanEntity tgBan) {
        view.sendBanReply(chatId, tgBan);
    }

    @Override
    public void commandRequest(Update update, TgUserEntity tgUser) {
        sendPlatformChoice(update, update.getMessage().getText());
        var data = update.getCallbackQuery();
        /*var userInfo = showUserInfo(update.getMessage().getText());
        view.sendUserInfo(update, userInfo);*/
    }

    @Override
    public void callbackRequest(Update update, TgUserEntity tgUser) {
        String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
        String data = update.getCallbackQuery().getData();
        Integer callbackMessageId = update.getCallbackQuery().getMessage().getMessageId();
        if (data.startsWith("userinfo:")){
            var loadingMessage = new LoadingMessage();
            loadingMessage.callbackMessageId = callbackMessageId;
            view.manageLoadingMessage(chatId, loadingMessage);
            //TODO query
        }
    }

    private void sendPlatformChoice(Update update, String username){
        UserPlatformChoice platformChoice = new UserPlatformChoice();
        platformChoice.username = username;
        platformChoice.buttons.add(new ButtonWithData("Twitch", "userinfo:twitch:"+username));
        platformChoice.buttons.add(new ButtonWithData("Trovo", "userinfo:trovo:"+username));
        platformChoice.buttons.add(new ButtonWithData("WASD", "userinfo:wasd:"+username));
        view.sendUserPlatformChoice(update, platformChoice);
    }

    private UserInfo showUserInfo(String userName){
        var userInfo = new UserInfo();
        return userInfo;
    }
}
