package com.streamcollector.app.admin.stages.dataViews;

import com.streamcollector.app.util.TimeUtil;
import com.streamcollector.app.database.entities.TgUserEntity;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TgUserView {
    public final StringProperty id = new SimpleStringProperty("");
    public final StringProperty tg_id = new SimpleStringProperty("");
    public final StringProperty name = new SimpleStringProperty("");
    public final StringProperty username = new SimpleStringProperty("");
    public final StringProperty language = new SimpleStringProperty("");
    public final StringProperty messagesTotal = new SimpleStringProperty("");
    public final StringProperty firstOnlineTime = new SimpleStringProperty("");
    public final StringProperty lastOnlineTime = new SimpleStringProperty("");

    public static TgUserView fromTgUser(TgUserEntity tgUser){
        var user = new TgUserView();

        user.id.set(tgUser.id.toString());
        user.tg_id.set(tgUser.tgId);
        user.name.set(String.format("%s %s", tgUser.lastName == null ? "" : tgUser.lastName, tgUser.firstName == null ? "" : tgUser.firstName).trim());
        user.username.set(tgUser.username == null ? "" : tgUser.username);
        user.language.set(tgUser.language);
        user.messagesTotal.set(tgUser.messagesTotal.toString());
        user.firstOnlineTime.set(TimeUtil.formatZoned(tgUser.firstOnlineTime));
        user.lastOnlineTime.set(TimeUtil.formatZoned(tgUser.lastOnlineTime));

        return user;
    }

    public String getId() {
        return id.get();
    }

    public StringProperty idProperty() {
        return id;
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public String getTg_id() {
        return tg_id.get();
    }

    public StringProperty tg_idProperty() {
        return tg_id;
    }

    public void setTg_id(String tg_id) {
        this.tg_id.set(tg_id);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getUsername() {
        return username.get();
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public String getLanguage() {
        return language.get();
    }

    public StringProperty languageProperty() {
        return language;
    }

    public void setLanguage(String language) {
        this.language.set(language);
    }

    public String getMessagesTotal() {
        return messagesTotal.get();
    }

    public StringProperty messagesTotalProperty() {
        return messagesTotal;
    }

    public void setMessagesTotal(String messagesTotal) {
        this.messagesTotal.set(messagesTotal);
    }

    public String getFirstOnlineTime() {
        return firstOnlineTime.get();
    }

    public StringProperty firstOnlineTimeProperty() {
        return firstOnlineTime;
    }

    public void setFirstOnlineTime(String firstOnlineTime) {
        this.firstOnlineTime.set(firstOnlineTime);
    }

    public String getLastOnlineTime() {
        return lastOnlineTime.get();
    }

    public StringProperty lastOnlineTimeProperty() {
        return lastOnlineTime;
    }

    public void setLastOnlineTime(String lastOnlineTime) {
        this.lastOnlineTime.set(lastOnlineTime);
    }
}
