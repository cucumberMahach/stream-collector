package service;

import admin.AdminApp;
import bot.AsyncBotBodyExecutor;
import bot.Bot;
import bot.BotDatabase;
import bot.BotStandardBody;
import bot.logic.BotStandardLogic;
import bot.view.BotStandardView;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

public class AdminService extends AbstractService{

    public AdminService() {
        super("admin", true, false);
    }

    @Override
    protected void work() {
        try {
            AdminApp.startApp(this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
