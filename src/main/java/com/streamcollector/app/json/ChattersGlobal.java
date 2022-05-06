package com.streamcollector.app.json;

import com.google.gson.annotations.SerializedName;

public class ChattersGlobal {
    @SerializedName("chatter_count")
    public int chatterCount;

    @SerializedName("chatters")
    public Chatters chatters;

    public int calcUsersCount(){
        int count = 0;
        for (var arr : chatters.arrMap.values()){
            count += arr.size();
        }
        return count;
    }

    public boolean isEqual(ChattersGlobal other){
        return (chatterCount == other.chatterCount &&
                calcUsersCount() == other.calcUsersCount() &&
                chatters.isEqual(other.chatters)
        );
    }
}
