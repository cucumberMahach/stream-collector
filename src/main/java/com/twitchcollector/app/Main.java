package com.twitchcollector.app;

import com.twitchcollector.app.console.ConsoleProvider;
import com.twitchcollector.app.grabber.GrabChannelResult;
import com.twitchcollector.app.grabber.TwitchGrabber;
import com.twitchcollector.app.logging.LogStatus;
import com.twitchcollector.app.logging.Logger;

import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Locale.setDefault(new Locale("ru", "RU"));
        java.util.logging.Logger.getLogger("org.hibernate").setLevel(Level.FINEST);
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
    }
}