package com.twitchcollector.app.grabber;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.twitchcollector.app.json.ChattersGlobal;
import com.twitchcollector.app.json.adapters.ZonedDateTimeAdapter;
import com.twitchcollector.app.util.DataUtil;
import com.twitchcollector.app.util.TimeUtil;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class GrabChannelResult {
    private static final Gson serializer = new GsonBuilder().registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeAdapter()).setPrettyPrinting().create();

    @SerializedName("chatters_global")
    public ChattersGlobal chattersGlobal;
    @SerializedName("channel_name")
    public String channelName;
    @SerializedName("timestamp")
    public ZonedDateTime timestamp = TimeUtil.getZonedNow();

    public Platform platform;

    @SerializedName("error")
    public GrabChannelError error = GrabChannelError.None;
    public transient GrabAbstractException exception = null;
    public transient Throwable unknownException = null;

    public transient GrabChannelResult previous = null;

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

    public static void serializeGrabsToFile(List<GrabChannelResult> grabs, String fileName) throws IOException {
        var str = serializer.toJson(grabs);
        Files.writeString(Paths.get(fileName), str);
    }

    public static List<GrabChannelResult> deserializeGrabsFromFile(String fileName) throws IOException {
        var str = Files.readString(Paths.get(fileName));
        Type type = new TypeToken<ArrayList<GrabChannelResult>>() {}.getType();
        return serializer.fromJson(str, type);
    }
}
