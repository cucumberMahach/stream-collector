package com.streamcollector.app.service;

import com.streamcollector.app.bot.AsyncBotBodyExecutor;
import com.streamcollector.app.bot.BotDatabase;
import com.streamcollector.app.bot.BotStandardBody;
import com.streamcollector.app.bot.logic.BotStandardLogic;
import com.streamcollector.app.bot.view.BotStandardView;
import com.streamcollector.app.settings.Settings;
import com.streamcollector.app.bot.Bot;
import com.streamcollector.app.tasks.TasksManager;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

public class BotService extends AbstractService{

    private final ArrayBlockingQueue<Update> queue = new ArrayBlockingQueue<Update>(1, true);
    private int threadsCount = 4;
    private final ArrayList<AsyncBotBodyExecutor> executors = new ArrayList<>();

    public BotService() {
        super("bot", true, false);
    }

    @Override
    protected void work() {
        Bot bot = new Bot(this, "TwitchCollectorBot", Settings.instance.getSettings().botTestAccount ? Settings.instance.getPrivateSettings().telegramTokenTest : Settings.instance.getPrivateSettings().telegramToken);
        TasksManager tasks = new TasksManager(this);

        try {
            bot.botConnect();
        } catch (Throwable e) {
            e.printStackTrace();
            return;
        }

        tasks.start();

        for (int thread = 0; thread < threadsCount; thread++){
            var executor = new AsyncBotBodyExecutor(this, String.format("Thread %d", thread+1));
            executor.setQueue(queue);
            executor.setBotBody(new BotStandardBody(new BotDatabase(), tasks));
            executor.getBotBody().setBot(bot);
            executor.getBotBody().setLogic(new BotStandardLogic());
            executor.getBotBody().getLogic().setBotView(new BotStandardView());
            executor.start();
            executors.add(executor);
        }

        try {
            while(true){
                tasks.update();
                Thread.sleep(10);
            }
        }catch (InterruptedException e){

        }

        destroyAllExecutors();
        tasks.stop();
        bot.getSession().stop();
    }

    private void destroyAllExecutors(){
        for (int i = 0; i < executors.size(); i++){
            executors.get(i).interrupt();
        }
        executors.clear();
    }

    public void addUpdateToQueue(Update update) {
        queue.add(update);
    }

    public int getThreadsCount() {
        return threadsCount;
    }

    public void setThreadsCount(int threadsCount) {
        this.threadsCount = threadsCount;
    }
}
