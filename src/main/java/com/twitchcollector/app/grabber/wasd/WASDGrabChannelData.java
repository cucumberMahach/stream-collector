package com.twitchcollector.app.grabber.wasd;

import com.twitchcollector.app.grabber.GrabChannelResult;
import com.twitchcollector.app.grabber.Platform;
import com.twitchcollector.app.json.Chatters;
import com.twitchcollector.app.json.ChattersGlobal;
import com.twitchcollector.app.util.TimeUtil;

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
            result.chattersGlobal.chatters.broadcaster = participants.owners.toArray(new String[0]);
            result.chattersGlobal.chatters.moderators = participants.moderators.toArray(new String[0]);
            result.chattersGlobal.chatters.viewers = participants.users.toArray(new String[0]);

            result.chattersGlobal.chatters.fillSetsFromArrays();
        }

        return result;
    }
}
