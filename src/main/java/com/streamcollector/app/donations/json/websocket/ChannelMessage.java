package com.streamcollector.app.donations.json.websocket;

public class ChannelMessage {
    public ChannelResult result;

    public boolean isCorrect(){
        return result != null && result.channel != null && !result.channel.isEmpty() && result.data != null && result.data.data != null && result.data.data.id != null;
    }
}
