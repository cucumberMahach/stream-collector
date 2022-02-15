package util.grabber;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import json.ChattersGlobal;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class GrabUtil {
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
        String data = executeGet(getChattersUrl(channelName));
        return createChattersGlobalObject(data);
    }

    public static ChattersGlobal createChattersGlobalObject(final String json){
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        try {
            return gson.fromJson(json, ChattersGlobal.class);
        }catch (JsonSyntaxException e){
            throw new GrabJsonException();
        }
    }

    public static String getChattersUrl(final String channelName) {
        return "https://tmi.twitch.tv/group/user/" + channelName + "/chatters";
    }
}
