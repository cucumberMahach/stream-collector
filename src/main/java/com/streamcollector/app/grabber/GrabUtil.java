package com.streamcollector.app.grabber;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.streamcollector.app.grabber.wasd.*;
import com.streamcollector.app.json.ChattersGlobal;
import com.streamcollector.app.grabber.trovo.TrovoGrabUsers;
import com.streamcollector.app.grabber.trovo.TrovoGrabViewers;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class GrabUtil {
    private static final Gson gson = new GsonBuilder().create();

    private static String executeGet(final String httpsUrl) {
        try {
            URL url = new URL(httpsUrl);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            //connection.setRequestMethod("GET");
            //connection.setRequestProperty("Client-I", "gp762nuuoqcoxypju8c569th9wz7q5");

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }catch (Exception e){
            throw new GrabIOException();
        }
    }

    public static ChattersGlobal getChattersGlobal(final String channelName){
        String data = executeGet(getTwitchChattersUrl(channelName));
        return createChattersGlobalObject(data);
    }

    public static ChattersGlobal createChattersGlobalObject(final String json){
        try {
            return gson.fromJson(json, ChattersGlobal.class);
        }catch (JsonSyntaxException e){
            throw new GrabJsonException();
        }
    }

    public static String parseWASDTokenJson(final String json) throws IOException {
        var obj = gson.fromJson(json, WASDToken.class);
        return obj.result;
    }

    public static long parseWASDChannelIDJson(final String json){
        var obj = gson.fromJson(json, WASDChannelIDGlobal.class);
        return obj.result.channel_id;
    }

    public static long parseWASDStreamIDJson(final String json){
        var obj = gson.fromJson(json, WASDStreamIDGlobal.class);
        return obj.result[0].media_container_streams[0].stream_id;
    }

    public static WASDGrabParticipants parseWASDParticipantsJson(final String json){
        var obj = gson.fromJson(json, WASDParticipantsGlobal.class);
        var participants = new WASDGrabParticipants();
        for (var p : obj.result){
            switch (p.user_channel_role){
                case "CHANNEL_MODERATOR":
                    participants.moderators.add(p.user_login);
                    break;
                case "CHANNEL_OWNER":
                    participants.owners.add(p.user_login);
                    break;
                default: // CHANNEL_USER
                    participants.users.add(p.user_login);
            }
        }
        return participants;
    }

    public static TrovoGrabUsers parseTrovoUsersJson(final String json){
        return gson.fromJson(json, TrovoGrabUsers.class);
    }

    public static TrovoGrabViewers parseTrovoViewers(final String json){
        return gson.fromJson(json, TrovoGrabViewers.class);
    }

    public static String getTwitchChattersUrl(final String channelName) {
        return "https://tmi.twitch.tv/group/user/" + channelName + "/chatters";
    }

    public static String getWASDTokenUrl(){
        return "https://wasd.tv/api/auth/chat-token";
    }

    public static String getWASDChannelIDUrl(final String channelName){
        return "https://wasd.tv/api/channels/nicknames/" + channelName;
    }

    public static String getWASDStreamIDUrl(final String channelID){
        return "https://wasd.tv/api/v2/media-containers?limit=1&offset=0&media_container_status=RUNNING,STOPPED&media_container_type=SINGLE&channel_id=" + channelID;
    }

    public static String getWASDParticipantsUrl(final String streamID, final int offset){
        return String.format("https://wasd.tv/api/chat/streams/%s/participants?limit=10000&offset=%d", streamID, offset);
    }

    public static String getTrovoGetUsersUrl(){
        return "https://open-api.trovo.live/openplatform/getusers";
    }

    public static String getTrovoViewersUrl(String channelId){
        return String.format("https://open-api.trovo.live/openplatform/channels/%s/viewers", channelId);
    }
}
