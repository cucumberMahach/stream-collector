package com.streamcollector.app.bot.view;

import com.streamcollector.app.bot.commands.LoadingMessage;
import com.streamcollector.app.bot.commands.UserInfo;
import com.streamcollector.app.bot.commands.UserPlatformChoice;
import com.streamcollector.app.util.TimeUtil;
import com.streamcollector.app.database.entities.TgBanEntity;
import com.streamcollector.app.database.entities.TgUserEntity;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class BotStandardView extends BotView{
    @Override
    public void sendStart(Update update, TgUserEntity tgUser) {
        botLogic.getBotBody().getBot().sendMsg(update.getMessage().getChatId().toString(), """
                &#128075; <b>Вас приветствует Twitch Collector Bot</b>

                Для получении информации о пользователе напишите его &#129313; <b><i>ник</i></b> или отправьте <b><i>ссылку</i></b> на канал &#129300;
                
                &#128591; <b>Пожалуйста</b>, не пробуйте использовать <b>SQL инъекции</b> на нашем боте. Спасибо""");
    }

    @Override
    public void sendHelp(Update update, TgUserEntity tgUser) {
        botLogic.getBotBody().getBot().sendMsg(update.getMessage().getChatId().toString(), """
                &#129488; <b>Помощь</b>
                
                Для получении информации о пользователе напишите его &#129313; <b><i>ник</i></b> или отправьте <b><i>ссылку</i></b> на канал &#129300;
                
                &#128591; <b>Пожалуйста</b>, не пробуйте использовать <b>SQL инъекции</b> на нашем боте. Спасибо""");
    }

    @Override
    public void sendEnableNotification(Update update) {
        botLogic.getBotBody().getBot().sendMsg(update.getMessage().getChatId().toString(), """
                &#128994; <b>Бот снова работает!</b>

                Вы получили это сообщение, потому что пробовали отправить запрос выключенному боту""");
    }

    @Override
    public void sendBanReply(String chatId, TgBanEntity tgBan) {
        var banUntil = TimeUtil.formatDurationDays(Duration.between(botLogic.getBotBody().getCurrentTime(), tgBan.untilTime));
        botLogic.getBotBody().getBot().sendMsg(chatId, String.format("""
                &#10060; <b>Вы забанены</b> &#10060;

                &#128221; Причина: <i>%s</i>
                &#128197; До конца бана осталось <b>%s</b>""", tgBan.reason, banUntil));
    }

    @Override
    public void sendUserInfo(Update update, UserInfo userInfo) {
        botLogic.getBotBody().getBot().sendSticker(update.getMessage().getChatId().toString(), "CAACAgIAAxkBAAEERyxiP1p-8qp8alVe51jr5SnwpQxLrgAChhsAAs7QiElhmJB0PqwN7yME", null);
    }

    @Override
    public void sendUserPlatformChoice(Update update, UserPlatformChoice platformChoice) {
        SendMessage msg = new SendMessage();
        msg.setText(String.format("""
                <b>Выберите платформу</b>
                Никнейм: %s
                """, platformChoice.username));
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        for (var btn : platformChoice.buttons){
            InlineKeyboardButton keyBtn = new InlineKeyboardButton(btn.title);
            keyBtn.setCallbackData(btn.data);
            buttons.add(keyBtn);
        }
        keyboard.add(buttons);
        inlineKeyboardMarkup.setKeyboard(keyboard);
        msg.setReplyMarkup(inlineKeyboardMarkup);
        botLogic.getBotBody().getBot().sendMsg(update.getMessage().getChatId().toString(), msg);
    }

    @Override
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
}