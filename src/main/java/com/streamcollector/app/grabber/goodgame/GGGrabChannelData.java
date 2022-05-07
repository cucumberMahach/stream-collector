package com.streamcollector.app.grabber.goodgame;

import com.streamcollector.app.grabber.GrabChannelResult;
import com.streamcollector.app.grabber.Platform;
import com.streamcollector.app.grabber.goodgame.http.GGChannelData;
import com.streamcollector.app.grabber.goodgame.websocket.GGUsersList;
import com.streamcollector.app.json.Chatters;
import com.streamcollector.app.json.ChattersGlobal;
import com.streamcollector.app.util.TimeUtil;

import java.time.ZonedDateTime;

public class GGGrabChannelData {
    public String channelName;
    public GGChannelData channelData;
    public GGUsersList usersList;
    public ZonedDateTime timestamp = TimeUtil.getZonedNow();
    public Throwable throwable;

    public GrabChannelResult toGrabChannelResult(){
        var result = new GrabChannelResult();
        result.channelName = channelName;
        result.platform = Platform.GoodGame;
        result.timestamp = timestamp;

        if (!isError()) {
            if (usersList == null){
                throwable = new Exception("usersList == null");
            }else {
                result.chattersGlobal = new ChattersGlobal();
                result.chattersGlobal.chatterCount = usersList.data.users.size();

                result.chattersGlobal.chatters = new Chatters();
                for (var user : usersList.data.users) {
                    if (user.rights == null || user.rights == 0) {
                        result.chattersGlobal.chatters.viewers.add(user.name);
                    } else if (user.rights == 10) {
                        result.chattersGlobal.chatters.stream_moder.add(user.name);
                    } else if (user.rights == 20) {
                        result.chattersGlobal.chatters.broadcaster.add(user.name);
                    } else if (user.rights == 30) {
                        result.chattersGlobal.chatters.moderators.add(user.name);
                    } else if (user.rights == 40) {
                        result.chattersGlobal.chatters.smoderator.add(user.name);
                    } else if (user.rights == 50) {
                        result.chattersGlobal.chatters.admins.add(user.name);
                    }
                }
                result.chattersGlobal.chatters.fillSetsAndConstructMaps();
            }
        }

        result.setError(throwable);
        return result;
    }

    public boolean isError(){
        return throwable != null;
    }
}
