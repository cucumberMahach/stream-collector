package com.streamcollector.app.grabber.goodgame.websocket;

import java.util.ArrayList;
import java.util.List;

public class GGUsersListData {
    public String channel_id;
    public Long clients_in_channel;
    public Long users_in_channel;
    public List<GGUser> users = new ArrayList<>();
}
