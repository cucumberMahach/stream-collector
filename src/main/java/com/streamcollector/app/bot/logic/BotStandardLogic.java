package com.streamcollector.app.bot.logic;

import com.google.gson.GsonBuilder;
import com.streamcollector.app.bot.BotStandardBody;
import com.streamcollector.app.bot.commands.ButtonWithData;
import com.streamcollector.app.bot.commands.LoadingMessage;
import com.streamcollector.app.bot.commands.UserInfo;
import com.streamcollector.app.bot.commands.UserPlatformChoice;
import com.streamcollector.app.bot.view.BotStandardView;
import com.streamcollector.app.database.entities.TgBanEntity;
import com.streamcollector.app.database.entities.TgUserEntity;
import com.streamcollector.app.grabber.Platform;
import com.streamcollector.app.tasks.database.results.TopViewsByUserItem;
import com.streamcollector.app.tasks.task.Task;
import com.streamcollector.app.tasks.task.TaskType;
import com.streamcollector.app.util.TimeUtil;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

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
            var splitted = data.split(":");
            if (splitted.length != 3){
                // error;
                return;
            }
            var platform = Platform.fromNameInDB(splitted[1]);
            if (platform == null){
                //error
                return;
            }
            var username = splitted[2];

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
                var deleteCmd = new DeleteMessage();
                deleteCmd.setChatId(chatId);
                deleteCmd.setMessageId(task.loadingMessage.callbackMessageId);
                try {
                    getBotBody().getBot().execute(deleteCmd);
                }catch (TelegramApiException e){
                    e.printStackTrace();
                }

                var topViews = (List<TopViewsByUserItem>) task.results.get("topViews");

                StringBuilder builder = new StringBuilder();
                builder.append(String.format("""
                        <b>Информация по пользователю:</b> <code>%s</code>
                        <b>Платформа:</b> <code>%s</code>
                        
                        <i>Топ 10 каналов по просмотрам за последний месяц</i>
                        """, task.parameters.get("username"), ((Platform)task.parameters.get("platform")).getShowName()));
                for (int i = 0; i < topViews.size(); i++){
                    var item = topViews.get(i);
                    builder.append(String.format("<b>%d.</b> <code>%s</code> (<b>%s</b>)\n", i + 1, item.channelName, TimeUtil.formatDurationDays(item.getDuration())));
                }

                var msg = new SendMessage();
                msg.setText(builder.toString());
                getBotBody().getBot().sendMsg(chatId, msg);
            };

            getBotBody().getTasksManager().executeTask(task);

        }else if (data.startsWith("menu:")){
            if (data.equals("menu:account")){
                view.sendAccountReply(chatId, tgUser);
            }
        }
    }

    private void sendPlatformChoice(Update update, String username){
        UserPlatformChoice platformChoice = new UserPlatformChoice();
        platformChoice.username = username;

        var line1 = new ArrayList<ButtonWithData>();
        line1.add(new ButtonWithData("\uD83E\uDD2C Twitch", String.format("userinfo:%s:%s", Platform.Twitch.getNameInDB(), username)));
        line1.add(new ButtonWithData("\u267F Trovo", String.format("userinfo:%s:%s", Platform.Trovo.getNameInDB(), username)));

        var line2 = new ArrayList<ButtonWithData>();
        line2.add(new ButtonWithData("\uD83E\uDD22 GoodGame", String.format("userinfo:%s:%s", Platform.GoodGame.getNameInDB(), username)));
        line2.add(new ButtonWithData("\uD83D\uDCA9 WASD", String.format("userinfo:%s:%s", Platform.WASD.getNameInDB(), username)));

        platformChoice.lines.add(line1);
        platformChoice.lines.add(line2);
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
