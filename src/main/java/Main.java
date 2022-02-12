import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Main {
    private static String executeGet(final String httpsUrl) {
        try {
            URL url = new URL(httpsUrl);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            //connection.setRequestMethod("GET");
            //connection.setRequestProperty("Client-I", "gp762nuuoqcoxypju8c569th9wz7q5");

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
        /*java.util.logging.Logger.getLogger("org.hibernate").setLevel(Level.OFF);

        StatelessSession session = database.DatabaseUtil.getStateLessSession();

        Transaction tx = session.beginTransaction();

        for (int i = 1; i <= 100000; i++) {
            try {
                database.entities.TestEntity entity = new database.entities.TestEntity();
                entity.setS("qweqweqwe"+i);
                session.insert(entity);
            }catch (Exception e){}
        }

        tx.commit();

        session.close();
        database.DatabaseUtil.shutdown();*/

        StatelessSession session = database.DatabaseUtil.getStateLessSession();
    }
}