package com.twitchcollector.app;

import com.twitchcollector.app.console.ConsoleProvider;
import com.twitchcollector.app.grabber.Grabber;
import com.twitchcollector.app.grabber.Platform;
import com.twitchcollector.app.logging.LogStatus;
import com.twitchcollector.app.logging.Logger;
import com.twitchcollector.app.util.Pair;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {
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
    }
}