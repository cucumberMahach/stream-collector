package com.streamcollector.app.donations.json.connect;

import java.util.ArrayList;
import java.util.List;

public class SubscribeRequest {
    public List<String> channels = new ArrayList<>();
    public String client;

    public void addAlertChannel(Long userId){
        channels.add(String.format("$alerts:donation_%d", userId));
    }
}
