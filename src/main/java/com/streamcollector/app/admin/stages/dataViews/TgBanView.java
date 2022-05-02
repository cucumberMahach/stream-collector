package com.streamcollector.app.admin.stages.dataViews;

import com.streamcollector.app.util.TimeUtil;
import com.streamcollector.app.database.entities.TgBanEntity;
import com.streamcollector.app.database.entities.TgUserEntity;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.Duration;
import java.time.ZonedDateTime;

public class TgBanView {
    public final StringProperty ban_id = new SimpleStringProperty("");
    public final StringProperty user_id = new SimpleStringProperty("");
    public final StringProperty reason = new SimpleStringProperty("");
    public final StringProperty fromTime = new SimpleStringProperty("");
    public final StringProperty untilTime = new SimpleStringProperty("");
    public final StringProperty duration = new SimpleStringProperty("");
    public final StringProperty remain = new SimpleStringProperty("");

    public ZonedDateTime fromTimeZoned;
    public ZonedDateTime untilTimeZoned;

    public static TgBanView fromTgBan(TgBanEntity tgBan, ZonedDateTime currentTime){
        var banLine = new TgBanView();
        banLine.ban_id.set(tgBan.id.toString());
        banLine.user_id.set(tgBan.tgUser.id.toString());
        banLine.reason.set(tgBan.reason);
        banLine.fromTime.set(TimeUtil.formatZoned(tgBan.fromTime));
        banLine.untilTime.set(TimeUtil.formatZoned(tgBan.untilTime));
        banLine.fromTimeZoned = tgBan.fromTime;
        banLine.untilTimeZoned = tgBan.untilTime;
        banLine.duration.set(TimeUtil.formatDurationDays(Duration.between(tgBan.fromTime, tgBan.untilTime)));
        String remain = "";
        if (tgBan.untilTime.isAfter(currentTime)){
            remain = TimeUtil.formatDurationDays(Duration.between(currentTime, tgBan.untilTime));
        }
        banLine.remain.set(remain);
        return banLine;
    }

    public TgBanEntity toTgBan(){
        var tgBan = new TgBanEntity();
        tgBan.id = Long.parseUnsignedLong(ban_id.get());
        tgBan.tgUser = new TgUserEntity();
        tgBan.tgUser.id = Long.parseUnsignedLong(user_id.get());
        tgBan.reason = reason.get();
        tgBan.fromTime = fromTimeZoned;
        tgBan.untilTime = untilTimeZoned;
        return tgBan;
    }

    public String getBan_id() {
        return ban_id.get();
    }

    public StringProperty ban_idProperty() {
        return ban_id;
    }

    public void setBan_id(String ban_id) {
        this.ban_id.set(ban_id);
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

    public String getReason() {
        return reason.get();
    }

    public StringProperty reasonProperty() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason.set(reason);
    }

    public String getFromTime() {
        return fromTime.get();
    }

    public StringProperty fromTimeProperty() {
        return fromTime;
    }

    public void setFromTime(String fromTime) {
        this.fromTime.set(fromTime);
    }

    public String getUntilTime() {
        return untilTime.get();
    }

    public StringProperty untilTimeProperty() {
        return untilTime;
    }

    public void setUntilTime(String untilTime) {
        this.untilTime.set(untilTime);
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

    public String getRemain() {
        return remain.get();
    }

    public StringProperty remainProperty() {
        return remain;
    }

    public void setRemain(String remain) {
        this.remain.set(remain);
    }
}
