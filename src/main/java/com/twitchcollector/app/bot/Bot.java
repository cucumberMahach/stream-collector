package com.twitchcollector.app.bot;

import com.twitchcollector.app.logging.LogStatus;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import com.twitchcollector.app.service.BotService;
import com.twitchcollector.app.util.DataUtil;
import com.twitchcollector.app.util.TimeUtil;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Bot extends TelegramLongPollingBot {
    private final int RECONNECT_PAUSE = 3000;

    private final BotService service;
    private final String name;
    private final String token;
    private BotSession session;

    private final ZonedDateTime startTime;
    private Set<String> startupNotificationsSet = Collections.synchronizedSet(new HashSet<>());

    public Bot(BotService service, String name, String token){
        this.service = service;
        this.name = name;
        this.token = token;
        startTime = TimeUtil.getZonedNow();
    }

    public void botConnect() throws InterruptedException {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            session = telegramBotsApi.registerBot(this);
            service.writeLog(LogStatus.Success,"TelegramAPI запущен");
        } catch (Exception e) {
            service.writeLog(LogStatus.Error,"Невозможно подключиться. Пауза " + RECONNECT_PAUSE / 1000 + " сек. Ошибка: " + DataUtil.getStackTrace(e));
            Thread.sleep(RECONNECT_PAUSE);
            botConnect();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        service.addUpdateToQueue(update);
    }

    public synchronized boolean sendSticker(String chatId, String stickerId, Integer replyToMessageId) {
        SendSticker sticker = new SendSticker(chatId, new InputFile(stickerId));

        if (replyToMessageId != null)
            sticker.setReplyToMessageId(replyToMessageId);

        try {
            execute(sticker);
        } catch (TelegramApiException e) {
            service.writeLog(LogStatus.Warning, "Ошибка отправки стикера: " + DataUtil.getStackTrace(e));
            return false;
        }
        return true;
    }

    public synchronized boolean sendMsg(String chatId, String text) {
        SendMessage msg = new SendMessage();
        msg.enableMarkdown(true);
        msg.setChatId(chatId);
        msg.setText(text);
        msg.setParseMode("html");
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            service.writeLog(LogStatus.Error, "Ошибка отправки сообщения: " + DataUtil.getStackTrace(e));
            return false;
        }
        return true;
    }

    public BotSession getSession(){
        return session;
    }

    @Override
    public String getBotUsername() {
        return name;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public Set<String> getStartupNotificationsSet() {
        return startupNotificationsSet;
    }
}
