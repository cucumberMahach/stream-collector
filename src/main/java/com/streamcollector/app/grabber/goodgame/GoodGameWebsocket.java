package com.streamcollector.app.grabber.goodgame;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.streamcollector.app.grabber.goodgame.websocket.GGJoin;
import com.streamcollector.app.grabber.goodgame.websocket.GGUsersList;
import com.streamcollector.app.util.DataUtil;
import com.streamcollector.app.util.TimeUtil;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;

public class GoodGameWebsocket extends WebSocketClient {

    private static final Gson gson = new GsonBuilder().create();

    private final ArrayList<GGGrabChannelData> channels = new ArrayList<>();
    private boolean done = false;
    private final ArrayList<String> channelsJoined = new ArrayList<>();
    private final ArrayList<GGGrabChannelData> results = new ArrayList<>();

    public GoodGameWebsocket(){
        super(URI.create("wss://chat-1.goodgame.ru/chat2/"));
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        send("""
                {
                    "type": "auth",
                    "data": {
                        "user_id": 0
                    }
                }
                """);
        //System.out.println("onOpen");
    }

    @Override
    public void onMessage(String s) {
        //System.out.println("onMessage: " + s);
        if (s.contains("success_auth")){
            for (var ch : channels) {
                send(String.format("""
                        {
                            "type": "join",
                            "data": {
                                "channel_id": "%s",
                                "hidden": false
                            }
                        }
                        """, ch.channelData.id));
            }
        }else if (s.contains("success_join")){
            var join = gson.fromJson(s, GGJoin.class);
            channelsJoined.add(join.data.channelId);
            send(String.format("""
                    {
                        "type": "get_users_list2",
                        "data": {
                            "channel_id": "%s"
                        }
                    }
                    """, join.data.channelId));
        }else if (s.contains("users_list")){
            var users = gson.fromJson(s, GGUsersList.class);
            var channelName = channels.stream().filter(ch -> ch.channelData.id.toString().equals(users.data.channel_id)).findFirst().get();
            channelName.usersList = users;
            channelName.timestamp = TimeUtil.getZonedNow();
            results.add(channelName);
            if (results.size() == channelsJoined.size()){
                done = true;
            }
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        //System.out.println("onClose: " + s);
    }

    @Override
    public void onError(Exception e) {
        //System.out.println("onError: " + DataUtil.getStackTrace(e));
    }

    public ArrayList<GGGrabChannelData> getChannels() {
        return channels;
    }

    public ArrayList<GGGrabChannelData> getResults() {
        return results;
    }

    public boolean isDone() {
        return done;
    }
}
