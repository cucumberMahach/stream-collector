package com.streamcollector.app.grabber.goodgame.http;

import com.google.gson.annotations.SerializedName;

public class GGChannelData {
    public String request_key;
    public Long id;
    public String key;

    @SerializedName("is_broadcast")
    public boolean isBroadcast;

    @SerializedName("broadcast_started")
    public Long broadcastStarted;

    @SerializedName("broadcast_end")
    public Long broadcastEnd;

    public String url;

    public String status;

    public String viewers;

    @SerializedName("player_viewers")
    public String playerViewers;

    @SerializedName("users_in_chat")
    public String usersInChat;

    public GGChannel channel;

    public GGLinks _links;
}
