package com.twitchcollector.app.grabber.trovo;

import com.google.gson.GsonBuilder;

import java.util.ArrayList;

public class TrovoRequestUsers {
    public ArrayList<String> user = new ArrayList<>();

    public String toJson(){
        var gson = new GsonBuilder().create();
        return gson.toJson(this);
    }
}
