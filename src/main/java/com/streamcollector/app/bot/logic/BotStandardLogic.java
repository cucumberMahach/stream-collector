package com.streamcollector.app.bot.logic;

import com.streamcollector.app.bot.BotStandardBody;
import com.streamcollector.app.bot.commands.ButtonWithData;
import com.streamcollector.app.bot.commands.LoadingMessage;
import com.streamcollector.app.bot.commands.UserPlatformChoice;
import com.streamcollector.app.bot.view.BotStandardView;
import com.streamcollector.app.database.entities.TgBanEntity;
import com.streamcollector.app.database.entities.TgUserEntity;
import com.streamcollector.app.grabber.Platform;
import com.streamcollector.app.tasks.task.Task;
import com.streamcollector.app.tasks.task.TaskType;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;

public class BotStandardLogic{
    protected BotStandardBody body;
    protected BotStandardView view;

    public void startRequest(Update update, TgUserEntity tgUser) {
        view.sendStart(update, tgUser);
    }

    public void menuRequest(Update update, TgUserEntity tgUser) {
        view.sendMenu(update, tgUser);
    }

    public void enableNotificationRequest(Update update) {
        view.sendEnableNotification(update);
    }

    public void banRequest(String chatId, TgBanEntity tgBan) {
        view.sendBanReply(chatId, tgBan);
    }

    public void accountRequest(String chatId, TgUserEntity tgUser){
        view.sendAccountReply(chatId, tgUser);
    }

    public void commandRequest(Update update, TgUserEntity tgUser) {
        sendPlatformChoice(update, update.getMessage().getText());
    }

    public void callbackRequest(Update update, TgUserEntity tgUser) {
        String chatId = update.getCallbackQuery().getMessage().getChatId().toString();
        String data = update.getCallbackQuery().getData();
        Integer callbackMessageId = update.getCallbackQuery().getMessage().getMessageId();

        AnswerCallbackQuery  answer = new AnswerCallbackQuery ();
        answer.setCallbackQueryId(update.getCallbackQuery().getId());
        try {
            body.getBot().execute(answer);
        }catch (TelegramApiException e){
            e.printStackTrace();
        }

        if (data.startsWith("userinfo:")){
            var parameters = data.split(":");
            createUserInfoTask(parameters, callbackMessageId, chatId);
            createUserLastViewsTask(parameters, chatId);
        }else if (data.startsWith("menu:")){
            if (data.equals("menu:account")){
                view.sendAccountReply(chatId, tgUser);
            }
        }else if (data.startsWith("user_search:")){
            var parameters = data.split(":");
            createUserSearchTask(parameters, callbackMessageId, chatId);
        }
    }

    private void createUserLastViewsTask(String[] parameters, String chatId){
        if (parameters.length != 3){
            // error;
            return;
        }
        var platform = Platform.fromNameInDB(parameters[1]);
        if (platform == null){
            //error
            return;
        }
        var username = parameters[2];
        if (username.isEmpty()){
            //error
            return;
        }

        Task task = new Task();
        task.type = TaskType.UserLastViews;
        task.chatId = chatId;

        task.parameters.put("platform", platform);
        task.parameters.put("username", username);
        task.parameters.put("maxCount", 10);
        task.onFinished = () -> {
            view.sendUserLastViewsResult(task);
        };

        getBotBody().getTasksManager().executeTask(task);
    }

    private void createUserSearchTask(String[] parameters, Integer callbackMessageId, String chatId){
        if (parameters.length != 2){
            // error;
            return;
        }
        String username = parameters[1];

        if (username.isEmpty()){
            //error
            return;
        }

        var loadingMessage = new LoadingMessage();
        loadingMessage.callbackMessageId = callbackMessageId;
        view.manageLoadingMessage(chatId, loadingMessage);

        Task task = new Task();
        task.loadingMessage = loadingMessage;
        task.type = TaskType.UserSearch;
        task.chatId = chatId;

        task.parameters.put("username", username);
        task.onFinished = () -> {
            view.sendUserSearchResult(task);
        };

        getBotBody().getTasksManager().executeTask(task);
    }

    private void createUserInfoTask(String[] parameters, Integer callbackMessageId, String chatId){
        if (parameters.length != 3){
            // error;
            return;
        }
        var platform = Platform.fromNameInDB(parameters[1]);
        if (platform == null){
            //error
            return;
        }
        var username = parameters[2];
        if (username.isEmpty()){
            //error
            return;
        }

        var loadingMessage = new LoadingMessage();
        loadingMessage.callbackMessageId = callbackMessageId;
        view.manageLoadingMessage(chatId, loadingMessage);

        Task task = new Task();
        task.loadingMessage = loadingMessage;
        task.type = TaskType.UserInfo;
        task.chatId = chatId;

        task.parameters.put("platform", platform);
        task.parameters.put("username", username);
        task.onFinished = () -> {
            view.sendUserInfoResult(task);
        };

        getBotBody().getTasksManager().executeTask(task);
    }

    private void sendPlatformChoice(Update update, String message){
        if (message.length() < 3){
            view.sendError(update.getMessage().getChatId().toString(),"Длина запроса должна быть не меньше 3-х символов", null);
        }else {
            UserPlatformChoice platformChoice = new UserPlatformChoice();
            platformChoice.username = message;

            var line1 = new ArrayList<ButtonWithData>();
            line1.add(new ButtonWithData("\uD83E\uDD2C Twitch", String.format("userinfo:%s:%s", Platform.Twitch.getNameInDB(), message)));
            line1.add(new ButtonWithData("\u267F Trovo", String.format("userinfo:%s:%s", Platform.Trovo.getNameInDB(), message)));

            var line2 = new ArrayList<ButtonWithData>();
            line2.add(new ButtonWithData("\uD83E\uDD22 GoodGame", String.format("userinfo:%s:%s", Platform.GoodGame.getNameInDB(), message)));
            line2.add(new ButtonWithData("\uD83D\uDCA9 WASD", String.format("userinfo:%s:%s", Platform.WASD.getNameInDB(), message)));

            var line3 = new ArrayList<ButtonWithData>();
            line3.add(new ButtonWithData("Возможные пользователи", String.format("user_search:%s", message)));

            platformChoice.lines.add(line1);
            platformChoice.lines.add(line2);
            platformChoice.lines.add(line3);
            view.sendUserPlatformChoice(update, platformChoice);
        }
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
