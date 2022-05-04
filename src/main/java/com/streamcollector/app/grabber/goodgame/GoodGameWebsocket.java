package com.streamcollector.app.grabber.goodgame;

import com.streamcollector.app.util.DataUtil;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class GoodGameWebsocket extends WebSocketClient {

    public GoodGameWebsocket(){
        super(URI.create("wss://chat.goodgame.ru/chat/websocket"));
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
        System.out.println("onOpen");
    }

    @Override
    public void onMessage(String s) {
        System.out.println("onMessage: " + s);
        if (s.contains("success_auth")){
            /*send("""
                    {
                        "type": "get_channels_list",
                        "data": {
                            "start": 0,
                            "count": 50
                        }
                    }
                    """);*/

            send("""
                    {
                        "type": "join",
                        "data": {
                            "channel_id": "5",
                            "hidden": false
                        }
                    }
                    """);
        }

        if (s.contains("success_join")){
            send("""
                    {
                        "type": "get_users_list2",
                        "data": {
                            "channel_id": "5"
                        }
                    }
                    """);
            System.out.println("USERS");
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        System.out.println("onClose: " + s);
    }

    @Override
    public void onError(Exception e) {
        System.out.println("onError: " + DataUtil.getStackTrace(e));
    }
}
