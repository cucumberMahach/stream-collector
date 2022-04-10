package com.twitchcollector.app;

import com.twitchcollector.app.console.ConsoleProvider;
import com.twitchcollector.app.grabber.TwitchGrabber;
import com.twitchcollector.app.logging.LogStatus;
import com.twitchcollector.app.logging.Logger;

import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class Main {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Locale.setDefault(new Locale("ru", "RU"));
        java.util.logging.Logger.getLogger("org.hibernate").setLevel(Level.WARNING);
        Logger.instance.writeLog(LogStatus.Success, "Запуск");
        ConsoleProvider.instance.startConsole();
        Logger.instance.writeLog(LogStatus.Success, "Завершение работы");
        System.exit(0);

        /*while (true){
            TwitchGrabber tg = new TwitchGrabber();
            tg.getChannelsToGrab().add("bratishkinoff");
            tg.getChannelsToGrab().add("mazellovvv");
            tg.getChannelsToGrab().add("juice" );
            tg.getChannelsToGrab().add("blackufa");
            tg.startGrabAsyncHttp();
            System.out.println("grabbed");
        }*/
    }
}