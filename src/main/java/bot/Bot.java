package bot;

import logging.LogStatus;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import service.AbstractService;
import util.DataUtil;
import util.TimeUtil;

import java.sql.Time;
import java.time.ZonedDateTime;
import java.util.Date;

public class Bot extends TelegramLongPollingBot {
    private final int RECONNECT_PAUSE = 3000;

    private final AbstractService service;
    private final String name;
    private final String token;
    private BotSession session;

    private ZonedDateTime botStartTime;

    public Bot(AbstractService service, String name, String token){
        this.service = service;
        this.name = name;
        this.token = token;
        botStartTime = TimeUtil.getZonedNow();
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
        var msgTime = TimeUtil.getZonedFromUnix(update.getMessage().getDate());
        if (msgTime.isBefore(botStartTime))
            return;
        System.out.println(update.getMessage().getFrom().getLanguageCode());
        sendSticker(update.getMessage().getChatId().toString(), "CAACAgIAAxkBAAEERyxiP1p-8qp8alVe51jr5SnwpQxLrgAChhsAAs7QiElhmJB0PqwN7yME", null);
    }

    public synchronized void sendSticker(String chatId, String stickerId, Integer replyToMessageId) {
        SendSticker sticker = new SendSticker(chatId, new InputFile(stickerId));

        if (replyToMessageId != null)
            sticker.setReplyToMessageId(replyToMessageId);

        try {
            execute(sticker);
        } catch (TelegramApiException e) {
            service.writeLog(LogStatus.Warning, "Ошибка отправки стикера: " + DataUtil.getStackTrace(e));
        }
    }

    public synchronized void sendMsg(String chatId, String text) {
        SendMessage msg = new SendMessage();
        msg.enableMarkdown(true);
        msg.setChatId(chatId);
        msg.setText(text);

        try {
            execute(msg);
        } catch (TelegramApiException e) {
            service.writeLog(LogStatus.Error, "Ошибка отправки сообщения: " + DataUtil.getStackTrace(e));
        }
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
}
