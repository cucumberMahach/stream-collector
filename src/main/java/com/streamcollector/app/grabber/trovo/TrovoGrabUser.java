package com.streamcollector.app.grabber.trovo;

import com.google.gson.annotations.SerializedName;

public class TrovoGrabUser {
    @SerializedName("user_id")
    public String userId;

    @SerializedName("username")
    public String username;

    @SerializedName("nickname")
    public String nickname;

    @SerializedName("channel_id")
    public String channelId;
}
