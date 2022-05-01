package com.twitchcollector.app.grabber.trovo;

import com.google.gson.GsonBuilder;

public class TrovoRequestViewers {
    public long limit;
    public long cursor;

    public String toJson(){
        var gson = new GsonBuilder().create();
        return gson.toJson(this);
    }
}
