package com.twitchcollector.app.grabber.trovo;

import com.twitchcollector.app.grabber.GrabChannelResult;
import com.twitchcollector.app.grabber.Platform;
import com.twitchcollector.app.json.Chatters;
import com.twitchcollector.app.json.ChattersGlobal;
import com.twitchcollector.app.util.TimeUtil;

import java.time.ZonedDateTime;
import java.util.ArrayList;

public class TrovoGrabChannelData {
    public String channelName;
    public TrovoGrabUser user;
    public TrovoGrabViewers viewers;
    public ZonedDateTime participantsTimestamp = TimeUtil.getZonedNow();

    public Throwable throwable = null;

    public boolean isError(){
        return throwable != null;
    }

    public boolean isUserError(){
        return user == null;
    }

    public void storeUser(TrovoGrabUsers users){
        var user = users.users.stream().filter(trovoGrabUser -> trovoGrabUser.username.equals(channelName)).findFirst();
        user.ifPresent(trovoGrabUser -> this.user = trovoGrabUser);
    }

    public GrabChannelResult toGrabChannelResult(){
        var result = new GrabChannelResult();
        result.channelName = channelName;
        result.setError(throwable);
        result.platform = Platform.Trovo;
        result.timestamp = participantsTimestamp;

        if (!isError() && viewers != null) {
            result.chattersGlobal = new ChattersGlobal();
            result.chattersGlobal.chatterCount = viewers.countAll();

            result.chattersGlobal.chatters = new Chatters();

            ArrayList<String> viewersFromAll = new ArrayList<>();

            if (viewers.chatters.containsKey("all"))
                viewersFromAll.addAll(viewers.chatters.get("all").viewers);

            if (viewers.chatters.containsKey("VIPS"))
                result.chattersGlobal.chatters.vips = viewers.chatters.get("VIPS").viewers.toArray(new String[0]);
            if (viewers.chatters.containsKey("admins"))
                result.chattersGlobal.chatters.admins = viewers.chatters.get("admins").viewers.toArray(new String[0]);
            if (viewers.chatters.containsKey("moderators"))
                result.chattersGlobal.chatters.moderators = viewers.chatters.get("moderators").viewers.toArray(new String[0]);
            if (viewers.chatters.containsKey("supermods"))
                result.chattersGlobal.chatters.global_mods = viewers.chatters.get("supermods").viewers.toArray(new String[0]);

            viewersFromAll.removeAll(viewers.chatters.get("VIPS").viewers);
            viewersFromAll.removeAll(viewers.chatters.get("admins").viewers);
            viewersFromAll.removeAll(viewers.chatters.get("moderators").viewers);
            viewersFromAll.removeAll(viewers.chatters.get("supermods").viewers);

            result.chattersGlobal.chatters.viewers = viewersFromAll.toArray(new String[0]);

            result.chattersGlobal.chatters.fillSetsFromArrays();
        }

        return result;
    }
}
