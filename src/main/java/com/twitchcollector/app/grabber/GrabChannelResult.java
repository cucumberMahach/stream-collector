package com.twitchcollector.app.grabber;

import com.twitchcollector.app.json.ChattersGlobal;
import com.twitchcollector.app.util.DataUtil;
import com.twitchcollector.app.util.TimeUtil;

import java.time.ZonedDateTime;

public class GrabChannelResult {
    public ChattersGlobal chattersGlobal;
    public String channelName;
    public ZonedDateTime timestamp = TimeUtil.getZonedNow();

    public GrabChannelError error = GrabChannelError.None;
    public GrabAbstractException exception = null;
    public Throwable unknownException = null;

    public GrabChannelResult previous = null;

    public void setError(Throwable ex){
        exception = null;
        unknownException = null;
        error = GrabChannelError.None;

        if (ex == null){
            return;
        }

        if (!(ex instanceof GrabAbstractException)){
            unknownException = ex;
            error = GrabChannelError.Unknown;
            return;
        }

        if (ex instanceof GrabIOException){
            error = GrabChannelError.IOException;
        }else if (ex instanceof GrabJsonException){
            error = GrabChannelError.JsonSyntaxException;
        }else{
            error = GrabChannelError.OtherGrabException;
        }

        exception = (GrabAbstractException) ex;
    }

    public String getError(){
        if (error == GrabChannelError.None)
            return "";

        String s = error.name() + " -> ";
        if (error == GrabChannelError.Unknown && unknownException != null){
            return s + unknownException.getMessage() + " " + DataUtil.getStackTrace(unknownException);
        }else if (exception != null){
            return s + exception.getMessage() + " " + DataUtil.getStackTrace(exception);
        }

        return "";
    }

    public boolean isError(){
        return error != GrabChannelError.None || chattersGlobal == null;
    }
}
