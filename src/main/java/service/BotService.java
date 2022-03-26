package service;

import bot.Bot;
import logging.LogStatus;
import logging.Logger;

public class BotService extends AbstractService{
    public BotService() {
        super("bot", false);
    }

    @Override
    protected void work() {
        Bot bot = new Bot(this, "TwitchCollectorBot", "5144285675:AAFsbq_JZpfmloBM_3acVnP03DR3kW3DUj0");
        try {
            bot.botConnect();
        } catch (Throwable e) {
            e.printStackTrace();
            return;
        }
        try {
            while(true){
                Thread.sleep(100);
            }
        }catch (Throwable e){
            bot.getSession().stop();
        }
    }
}
