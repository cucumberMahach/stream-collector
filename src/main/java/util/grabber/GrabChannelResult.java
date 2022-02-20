package util.grabber;

import json.ChattersGlobal;
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
            return s + unknownException.getMessage() + " " + Arrays.toString(unknownException.getStackTrace()); //TODO Не выводит ошибку
        }else if (exception != null){
            return s + exception.getMessage() + " " + Arrays.toString(exception.getStackTrace());
        }

        return "";
    }

    public boolean isError(){
        return error != GrabChannelError.None || chattersGlobal == null;
    }
}
