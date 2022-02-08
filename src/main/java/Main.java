import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class Main {
    private static String executeGet(final String httpsUrl) {
        try {
            URL url = new URL(httpsUrl);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            try(BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void get(){
        long startTime = System.currentTimeMillis();
        String data = executeGet("https://tmi.twitch.tv/group/user/jesusavgn/chatters");
        long endTime = System.currentTimeMillis();
        System.out.println(endTime-startTime + " ms");

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        ChattersGlobal chattersGlobal = gson.fromJson(data, ChattersGlobal.class);

        System.out.println(System.currentTimeMillis()-endTime + " ms");
        System.out.println("Chatters count: " + chattersGlobal.chatterCount);
        System.out.println("Viewers: " + chattersGlobal.chatters.viewers.length);
        System.out.println("Moderators: " + chattersGlobal.chatters.moderators.length);
    }

    public static void main(String[] args) {
        Session session = DatabaseUtil.getSession();
        Transaction tx = session.beginTransaction();

        for (int i = 1; i <= 1000; i++) {
            TestEntity entity = new TestEntity();
            entity.setS("qweqweqwe"+i);
            session.save(entity);
        }

        tx.commit();

        session.close();
        DatabaseUtil.shutdown();
    }
}