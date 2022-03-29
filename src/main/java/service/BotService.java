package service;

import bot.AsyncBotBodyExecutor;
import bot.Bot;
import bot.BotDatabase;
import bot.BotStandardBody;
import bot.logic.BotStandardLogic;
import bot.view.BotStandardView;
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
        Bot bot = new Bot(this, "TwitchCollectorBot", "5144285675:AAFsbq_JZpfmloBM_3acVnP03DR3kW3DUj0");

        try {
            bot.botConnect();
        } catch (Throwable e) {
            e.printStackTrace();
            return;
        }

        for (int thread = 0; thread < threadsCount; thread++){
            var executor = new AsyncBotBodyExecutor(this, String.format("Thread %d", thread+1));
            executor.setQueue(queue);
            executor.setBotBody(new BotStandardBody(new BotDatabase()));
            executor.getBotBody().setBot(bot);
            executor.getBotBody().setLogic(new BotStandardLogic());
            executor.getBotBody().getLogic().setBotView(new BotStandardView());
            executor.start();
            executors.add(executor);
        }

        try {
            while(true){
                Thread.sleep(100);
            }
        }catch (InterruptedException e){
            destroyAllExecutors();
            bot.getSession().stop();
        }
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
