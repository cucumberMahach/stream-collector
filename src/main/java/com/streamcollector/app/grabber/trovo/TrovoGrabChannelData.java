package com.streamcollector.app.grabber.trovo;

import com.streamcollector.app.grabber.GrabChannelResult;
import com.streamcollector.app.grabber.Platform;
import com.streamcollector.app.json.Chatters;
import com.streamcollector.app.json.ChattersGlobal;
import com.streamcollector.app.util.TimeUtil;

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
        result.platform = Platform.Trovo;
        result.timestamp = participantsTimestamp;

        if (!isError()) {
            if (viewers == null) {
                throwable = new Exception("viewers == null");
            }else {
                result.chattersGlobal = new ChattersGlobal();
                result.chattersGlobal.chatterCount = viewers.countAll();

                result.chattersGlobal.chatters = new Chatters();

                ArrayList<String> viewersFromAll = new ArrayList<>();

                if (viewers.chatters.containsKey("all"))
                    viewersFromAll.addAll(viewers.chatters.get("all").viewers);

                if (viewers.chatters.containsKey("VIPS")) {
                    result.chattersGlobal.chatters.vips.addAll(viewers.chatters.get("VIPS").viewers);
                    viewersFromAll.removeAll(viewers.chatters.get("VIPS").viewers);
                }
                if (viewers.chatters.containsKey("ace")) {
                    result.chattersGlobal.chatters.ace.addAll(viewers.chatters.get("ace").viewers);
                    viewersFromAll.removeAll(viewers.chatters.get("ace").viewers);
                }
                if (viewers.chatters.containsKey("aceplus")) {
                    result.chattersGlobal.chatters.aceplus.addAll(viewers.chatters.get("aceplus").viewers);
                    viewersFromAll.removeAll(viewers.chatters.get("aceplus").viewers);
                }
                if (viewers.chatters.containsKey("admins")) {
                    result.chattersGlobal.chatters.admins.addAll(viewers.chatters.get("admins").viewers);
                    viewersFromAll.removeAll(viewers.chatters.get("admins").viewers);
                }
                if (viewers.chatters.containsKey("creators")) {
                    result.chattersGlobal.chatters.creators.addAll(viewers.chatters.get("creators").viewers);
                    viewersFromAll.removeAll(viewers.chatters.get("creators").viewers);
                }
                if (viewers.chatters.containsKey("editors")) {
                    result.chattersGlobal.chatters.editors.addAll(viewers.chatters.get("editors").viewers);
                    viewersFromAll.removeAll(viewers.chatters.get("editors").viewers);
                }
                if (viewers.chatters.containsKey("followers")) {
                    result.chattersGlobal.chatters.followers.addAll(viewers.chatters.get("followers").viewers);
                    viewersFromAll.removeAll(viewers.chatters.get("followers").viewers);
                }
                if (viewers.chatters.containsKey("subscribers")) {
                    result.chattersGlobal.chatters.subscribers.addAll(viewers.chatters.get("subscribers").viewers);
                    viewersFromAll.removeAll(viewers.chatters.get("subscribers").viewers);
                }
                if (viewers.chatters.containsKey("moderators")) {
                    result.chattersGlobal.chatters.moderators.addAll(viewers.chatters.get("moderators").viewers);
                    viewersFromAll.removeAll(viewers.chatters.get("moderators").viewers);
                }
                if (viewers.chatters.containsKey("supermods")) {
                    result.chattersGlobal.chatters.smoderator.addAll(viewers.chatters.get("supermods").viewers);
                    viewersFromAll.removeAll(viewers.chatters.get("supermods").viewers);
                }
                if (viewers.chatters.containsKey("wardens")) {
                    result.chattersGlobal.chatters.staff.addAll(viewers.chatters.get("wardens").viewers);
                    viewersFromAll.removeAll(viewers.chatters.get("wardens").viewers);
                }

                result.chattersGlobal.chatters.viewers.addAll(viewersFromAll);

                result.chattersGlobal.chatters.fillSetsAndConstructMaps();
            }
        }

        result.setError(throwable);
        return result;
    }
}
