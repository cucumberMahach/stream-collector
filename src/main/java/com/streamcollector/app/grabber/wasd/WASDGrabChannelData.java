package com.streamcollector.app.grabber.wasd;

import com.streamcollector.app.grabber.GrabChannelResult;
import com.streamcollector.app.grabber.Platform;
import com.streamcollector.app.json.Chatters;
import com.streamcollector.app.json.ChattersGlobal;
import com.streamcollector.app.util.TimeUtil;

import java.time.ZonedDateTime;

public class WASDGrabChannelData {
    public String channelName = null;
    public Long channelID = null;
    public Long streamID = null;
    public WASDGrabParticipants participants = null;
    public ZonedDateTime participantsTimestamp = TimeUtil.getZonedNow();

    public Throwable throwable = null;

    public boolean isError(){
        return throwable != null;
    }

    public GrabChannelResult toGrabChannelResult(){
        var result = new GrabChannelResult();
        result.channelName = channelName;
        result.setError(throwable);
        result.platform = Platform.WASD;
        result.timestamp = participantsTimestamp;

        if (!isError() && participants != null) {
            result.chattersGlobal = new ChattersGlobal();
            result.chattersGlobal.chatterCount = participants.countAll();

            result.chattersGlobal.chatters = new Chatters();
            result.chattersGlobal.chatters.broadcaster.addAll(participants.owners);
            result.chattersGlobal.chatters.moderators.addAll(participants.moderators);
            result.chattersGlobal.chatters.viewers.addAll(participants.users);

            result.chattersGlobal.chatters.fillSetsAndConstructMaps();
        }

        return result;
    }
}
