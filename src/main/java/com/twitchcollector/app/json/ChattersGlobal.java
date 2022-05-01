package com.twitchcollector.app.json;

import com.google.gson.annotations.SerializedName;

public class ChattersGlobal {
    @SerializedName("chatter_count")
    public int chatterCount;

    @SerializedName("chatters")
    public Chatters chatters;

    public int calcUsersCount(){
        return chatters.viewers.length + chatters.moderators.length + chatters.admins.length + chatters.vips.length + chatters.broadcaster.length + chatters.staff.length + chatters.global_mods.length;
    }

    public boolean isEqual(ChattersGlobal other){
        return (chatterCount == other.chatterCount &&
                calcUsersCount() == other.calcUsersCount() &&
                chatters.isEqual(other.chatters)
        );
    }
}
