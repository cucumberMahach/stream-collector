package com.streamcollector.app.bot.logic;

import com.streamcollector.app.bot.BotStandardBody;
import com.streamcollector.app.bot.commands.ButtonWithData;
import com.streamcollector.app.bot.commands.LoadingMessage;
import com.streamcollector.app.bot.commands.UserInfo;
import com.streamcollector.app.bot.commands.UserPlatformChoice;
import com.streamcollector.app.bot.view.BotStandardView;
import com.streamcollector.app.database.entities.TgBanEntity;
import com.streamcollector.app.database.entities.TgUserEntity;
import org.telegram.telegrambots.meta.api.objects.Update;

public class BotStandardLogic{
    protected BotStandardBody body;
    protected BotStandardView view;

    public void startRequest(Update update, TgUserEntity tgUser) {
        view.sendStart(update, tgUser);
    }

    public void helpRequest(Update update, TgUserEntity tgUser) {
        view.sendHelp(update, tgUser);
    }

    public void enableNotificationRequest(Update update) {
        view.sendEnableNotification(update);
    }

    public void banRequest(String chatId, TgBanEntity tgBan) {
        view.sendBanReply(chatId, tgBan);
    }

    public void accountRequest(Update update, TgUserEntity tgUser){
        view.sendAccountReply(update, tgUser);
    }

    public void commandRequest(Update update, TgUserEntity tgUser) {
        sendPlatformChoice(update, update.getMessage().getText());
        var data = update.getCallbackQuery();
        /*var userInfo = showUserInfo(update.getMessage().getText());
        view.sendUserInfo(update, userInfo);*/
    }

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

    public void setBotBody(BotStandardBody body) {
        this.body = body;
    }

    public BotStandardBody getBotBody() {
        return body;
    }

    public BotStandardView getBotView() {
        return view;
    }

    public void setBotView(BotStandardView view) {
        this.view = view;
        view.setBotLogic(this);
    }
}
