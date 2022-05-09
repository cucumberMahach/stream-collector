package com.streamcollector.app.bot.view;

import com.streamcollector.app.bot.commands.LoadingMessage;
import com.streamcollector.app.bot.commands.UserPlatformChoice;
import com.streamcollector.app.bot.logic.BotStandardLogic;
import com.streamcollector.app.database.entities.TgBanEntity;
import com.streamcollector.app.database.entities.TgUserEntity;
import com.streamcollector.app.util.TimeUtil;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class BotStandardView{
    protected BotStandardLogic botLogic;

    private void addGlobalKeyboard(SendMessage message){
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add("Показать меню");
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);
    }

    private void send(String chatId, SendMessage message){
        addGlobalKeyboard(message);
        botLogic.getBotBody().getBot().sendMsg(chatId, message);
    }

    private void sendClear(String chatId, SendMessage message){
        botLogic.getBotBody().getBot().sendMsg(chatId, message);
    }

    public void sendStart(Update update, TgUserEntity tgUser) {
        var msg = new SendMessage();
        msg.setText("""
                &#128075; <b>Вас приветствует Stream Deanon Bot</b>
                """);

        send(update.getMessage().getChatId().toString(), msg);
        sendMenu(update, tgUser);
    }

    public void sendMenu(Update update, TgUserEntity tgUser){
        var msg = new SendMessage();
        msg.setText("""
                &#129488; <b>Меню</b>
                
                Для получении информации о пользователе напишите его &#129313; <b><i>ник</i></b> или отправьте <b><i>ссылку</i></b> на канал &#129300;
                
                &#128591; <b>Пожалуйста</b>, не пробуйте использовать <b>SQL инъекции</b> на нашем боте. Спасибо""");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> buttons = new ArrayList<>();
        var btnAccount = new InlineKeyboardButton();
        btnAccount.setText("\uD83D\uDC51 Аккаунт");
        btnAccount.setCallbackData("menu:account");
        buttons.add(btnAccount);
        keyboard.add(buttons);

        inlineKeyboardMarkup.setKeyboard(keyboard);
        msg.setReplyMarkup(inlineKeyboardMarkup);
        sendClear(update.getMessage().getChatId().toString(), msg);
    }

    public void sendEnableNotification(Update update) {
        var msg = new SendMessage();
        msg.setText("""
                &#128994; <b>Бот снова работает!</b>

                Вы получили это сообщение, потому что пробовали отправить запрос выключенному боту""");
        send(update.getMessage().getChatId().toString(), msg);
    }

    public void sendBanReply(String chatId, TgBanEntity tgBan) {
        var banUntil = TimeUtil.formatDurationDays(Duration.between(botLogic.getBotBody().getCurrentTime(), tgBan.untilTime));
        var msg = new SendMessage();
        msg.setText(String.format("""
                &#10060; <b>Вы забанены</b> &#10060;

                &#128221; <b>Причина:</b> <code>%s</code>
                &#128197; <b>До конца бана осталось:</b> <code>%s</code>""", tgBan.reason.isEmpty() ? "нет" : tgBan.reason, banUntil));
        send(chatId, msg);
    }

    public void sendAccountReply(String chatId, TgUserEntity tgUser){
        long days = Duration.between(tgUser.firstOnlineTime, TimeUtil.getZonedNow()).toDays();
        SendMessage msg = new SendMessage();
        msg.setText(String.format("""
                <b>Мой аккаунт</b>
                <i>Вся необходимая информация о вашем профиле</i>

                &#127380; <b>Telegram ID:</b> <code>%s</code>
                &#127913; <b>Регистрация:</b> <code>%s (%d дней)</code>
                &#128273; <b>Уникальный ключ:</b> <code>%s</code>
                
                &#128182; <b>Баланс:</b> <code>%d₽</code>
                """, tgUser.tgId, TimeUtil.formatToUserDate(tgUser.firstOnlineTime), days, tgUser.donationKey, tgUser.balance));
        send(chatId, msg);
    }

    public void sendUserPlatformChoice(Update update, UserPlatformChoice platformChoice) {
        SendMessage msg = new SendMessage();
        msg.setText(String.format("""
                <b>Выберите платформу</b>
                &#129313; <b>Никнейм:</b> <code>%s</code>
                """, platformChoice.username));
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (var line : platformChoice.lines){
            List<InlineKeyboardButton> buttons = new ArrayList<>();
            for (var btn : line) {
                InlineKeyboardButton keyBtn = new InlineKeyboardButton(btn.title);
                keyBtn.setCallbackData(btn.data);
                buttons.add(keyBtn);
            }
            keyboard.add(buttons);
        }

        inlineKeyboardMarkup.setKeyboard(keyboard);
        msg.setReplyMarkup(inlineKeyboardMarkup);
        sendClear(update.getMessage().getChatId().toString(), msg);
    }

    public void manageLoadingMessage(String chatId, LoadingMessage loadingMessage) {
        var editMessage = new EditMessageReplyMarkup();
        editMessage.setChatId(chatId);
        editMessage.setMessageId(loadingMessage.callbackMessageId);

        var editText = new EditMessageText();
        editText.enableMarkdown(true);
        editText.setParseMode("html");
        editText.setChatId(chatId);
        editText.setMessageId(loadingMessage.callbackMessageId);
        editText.setText("""
                <b>Выполнение запроса, ожидайте...</b>
                """);

        try {
            botLogic.getBotBody().getBot().execute(editMessage);
            botLogic.getBotBody().getBot().execute(editText);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void setBotLogic(BotStandardLogic botLogic) {
        this.botLogic = botLogic;
    }
}
