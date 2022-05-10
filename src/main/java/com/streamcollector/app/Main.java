package com.streamcollector.app;

import com.streamcollector.app.console.ConsoleProvider;
import com.streamcollector.app.donations.DonationsHandler;
import com.streamcollector.app.donations.StandardDonationsHandler;
import com.streamcollector.app.grabber.Grabber;
import com.streamcollector.app.grabber.Platform;
import com.streamcollector.app.grabber.goodgame.GoodGameWebsocket;
import com.streamcollector.app.logging.LogStatus;
import com.streamcollector.app.logging.Logger;
import com.streamcollector.app.settings.Settings;
import com.streamcollector.app.util.Pair;
import org.glassfish.jersey.server.Uri;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class Main {
    public static void main(String[] args) throws Exception {
        Locale.setDefault(new Locale("ru", "RU"));
        java.util.logging.Logger.getLogger("org.hibernate").setLevel(Level.OFF);
        Logger.instance.writeLog(LogStatus.Success, "Запуск");
        ConsoleProvider.instance.startConsole();
        Logger.instance.writeLog(LogStatus.Success, "Завершение работы");
        System.exit(0);

        /*GrabChannelResult res = null;
        long startTime = 0;
        while (true){
            TwitchGrabber tg = new TwitchGrabber();
            tg.getChannelsToGrab().add("jesusavgn");
            tg.startGrabAsyncHttp();
            if (res == null) {
                res = tg.getResults().get(0);
                startTime = System.currentTimeMillis();
                System.out.println("inited");
            }
            if (res.chattersGlobal.chatterCount != tg.getResults().get(0).chattersGlobal.chatterCount){
                System.out.println("Diff: " + (System.currentTimeMillis()-startTime));
                res = tg.getResults().get(0);
                startTime = System.currentTimeMillis();
            }else{
                System.out.println("grabbed");
            }
        }*/

        /*var grabber = new Grabber();
        grabber.getChannelsToGrab().add(new Pair<>(Platform.WASD, "megaradio"));
        grabber.startGrabAsyncHttp();
        var result = grabber.getResults();
        System.exit(0);*/

        /*var grabber = new Grabber();
        grabber.getChannelsToGrab().add(new Pair<>(Platform.Trovo, "mob5ter"));
        grabber.startGrabAsyncHttp();
        var result = grabber.getResults();
        System.exit(0);*/

        /*var donat = new StandardDonationsHandler();
        donat.setBearer(Settings.instance.getPrivateSettings().donatBearer);
        donat.start();*/

        /*GoodGameWebsocket gg = new GoodGameWebsocket();
        gg.connectBlocking();*/

        /*var grabber = new Grabber();
        grabber.getChannelsToGrab().add(new Pair<>(Platform.GoodGame, "hell_girl"));
        grabber.getChannelsToGrab().add(new Pair<>(Platform.GoodGame, "Nikichar"));
        grabber.getChannelsToGrab().add(new Pair<>(Platform.GoodGame, "JosephStalin"));
        grabber.startGrabAsyncHttp();
        var result = grabber.getResults();
        System.exit(0);*/

        /*var data = Files.readAllLines(Paths.get("C:/Users/aleks/Desktop/si.html"));
        StringBuilder builder = new StringBuilder();
        for (var l : data){
            int index = l.indexOf("https://streaminside.ru/streamers/");
            if (index != -1){
                String s = l.substring(index + "https://streaminside.ru/streamers/".length(), l.indexOf("\"><img src=\"./STREAM INSIDE - Новости о стримерах"));
                builder.append(s + "\n");
            }
        }
        Files.writeString(Paths.get("C:/Users/aleks/Desktop/qq.txt"), builder.toString());*/

        /*var data = Files.readAllLines(Paths.get("C:/Users/aleks/Desktop/new-streamers-csv.txt"));
        var newdata = new ArrayList<String>();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < data.size(); i++){
            var d = data.get(i);
            data.remove(i);
            i--;
            boolean containts = false;
            for (var q : newdata){
                if (q.equals(d)){
                    containts = true;
                    break;
                }
            }
            if (!containts){
                newdata.add(d);
                builder.append(d + "\r\n");
            }
        }
        Files.writeString(Paths.get("C:/Users/aleks/Desktop/jj.txt"), builder.toString());*/
    }
}