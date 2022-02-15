package util.grabber;

import json.ChattersGlobal;

public class GrabChannelResult {
    public ChattersGlobal chattersGlobal;
    public String channelName;

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
}
