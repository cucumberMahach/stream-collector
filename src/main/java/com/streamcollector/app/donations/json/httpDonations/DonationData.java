package com.streamcollector.app.donations.json.httpDonations;

import com.google.gson.annotations.SerializedName;
import com.streamcollector.app.util.TimeUtil;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DonationData {
    public Long id;

    public String name;

    public String username;

    public String message;

    public Long amount;

    public String currency;

    @SerializedName("is_shown")
    public Integer isShown;

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("shownAt")
    public String shown_at;

    private static final transient DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ZonedDateTime getCreatedAt(boolean moscow){
        ZonedDateTime zoned = ZonedDateTime.parse(createdAt, formatter.withZone(ZoneId.of("UTC")));
        if (moscow){
            return zoned.withZoneSameInstant(TimeUtil.zoneId);
        }else{
            return zoned;
        }
    }
}
