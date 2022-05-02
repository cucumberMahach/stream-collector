package com.streamcollector.app.admin.stages.dataViews;

import com.streamcollector.app.util.TimeUtil;
import com.streamcollector.app.database.entities.TgHistoryEntity;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.Duration;

public class TgHistoryView {
    public final StringProperty history_id = new SimpleStringProperty("");
    public final StringProperty user_id = new SimpleStringProperty("");
    public final StringProperty message = new SimpleStringProperty("");
    public final StringProperty result = new SimpleStringProperty("");
    public final StringProperty messageTime = new SimpleStringProperty("");
    public final StringProperty requestTime = new SimpleStringProperty("");
    public final StringProperty answerTime = new SimpleStringProperty("");
    public final StringProperty duration = new SimpleStringProperty("");

    public static TgHistoryView fromTgHistory(TgHistoryEntity tgHistory){
        var tgHistoryView = new TgHistoryView();

        tgHistoryView.history_id.set(tgHistory.id.toString());
        tgHistoryView.user_id.set(tgHistory.tgUser.id.toString());
        tgHistoryView.message.set(tgHistory.message);
        tgHistoryView.result.set(tgHistory.result);
        tgHistoryView.messageTime.set(TimeUtil.formatZonedMs(tgHistory.messageTime));
        tgHistoryView.requestTime.set(TimeUtil.formatZonedMs(tgHistory.requestTime));
        tgHistoryView.answerTime.set(TimeUtil.formatZonedMs(tgHistory.answerTime));
        tgHistoryView.duration.set(TimeUtil.formatDurationHoursMs(Duration.between(tgHistory.messageTime, tgHistory.answerTime)));

        return tgHistoryView;
    }

    public String getHistory_id() {
        return history_id.get();
    }

    public StringProperty history_idProperty() {
        return history_id;
    }

    public void setHistory_id(String history_id) {
        this.history_id.set(history_id);
    }

    public String getUser_id() {
        return user_id.get();
    }

    public StringProperty user_idProperty() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id.set(user_id);
    }

    public String getMessage() {
        return message.get();
    }

    public StringProperty messageProperty() {
        return message;
    }

    public void setMessage(String message) {
        this.message.set(message);
    }

    public String getResult() {
        return result.get();
    }

    public StringProperty resultProperty() {
        return result;
    }

    public void setResult(String result) {
        this.result.set(result);
    }

    public String getMessageTime() {
        return messageTime.get();
    }

    public StringProperty messageTimeProperty() {
        return messageTime;
    }

    public void setMessageTime(String messageTime) {
        this.messageTime.set(messageTime);
    }

    public String getRequestTime() {
        return requestTime.get();
    }

    public StringProperty requestTimeProperty() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime.set(requestTime);
    }

    public String getAnswerTime() {
        return answerTime.get();
    }

    public StringProperty answerTimeProperty() {
        return answerTime;
    }

    public void setAnswerTime(String answerTime) {
        this.answerTime.set(answerTime);
    }

    public String getDuration() {
        return duration.get();
    }

    public StringProperty durationProperty() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration.set(duration);
    }
}
