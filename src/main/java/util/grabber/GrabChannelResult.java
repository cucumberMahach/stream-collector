package util.grabber;

import json.ChattersGlobal;
import util.DataUtil;
import util.TimeUtil;

import java.time.ZonedDateTime;
import java.util.Arrays;

public class GrabChannelResult {
    public ChattersGlobal chattersGlobal;
    public String channelName;
    public ZonedDateTime timestamp = TimeUtil.getZonedNow();

    public GrabChannelError error = GrabChannelError.None;
    public GrabAbstractException exception = null;
    public Throwable unknownException = null;

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
            var ps = DataUtil.getPrintStream();
            unknownException.printStackTrace(ps.first);
            ps.first.close();
            return s + unknownException.getMessage() + " " + ps.second.toString();
        }else if (exception != null){
            var ps = DataUtil.getPrintStream();
            exception.printStackTrace(ps.first);
            ps.first.close();
            return s + exception.getMessage() + " " + ps.second.toString();
        }

        return "";
    }

    public boolean isError(){
        return error != GrabChannelError.None || chattersGlobal == null;
    }
}
