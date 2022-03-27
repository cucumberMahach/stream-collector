package bot;

import logging.LogStatus;
import org.telegram.telegrambots.meta.api.objects.Update;
import service.AbstractService;

import java.util.concurrent.ArrayBlockingQueue;

public class AsyncBotAlgorithmExecutor extends Thread{

    protected BotAlgorithm botAlgorithm;
    protected ArrayBlockingQueue<Update> queue;
    protected AbstractService service;
    protected String name;

    public AsyncBotAlgorithmExecutor(AbstractService service, String name) {
        super();
        this.service = service;
        this.name = name;
    }

    @Override
    public void run() {
        if (botAlgorithm == null){
            service.writeLog(LogStatus.Error, String.format("AsyncBotAlgorithmExecutor названный %s - не задан алгоритм бота", name));
            return;
        }

        if (queue == null){
            service.writeLog(LogStatus.Error, String.format("AsyncBotAlgorithmExecutor названный %s - не задана очередь сообщений", name));
            return;
        }

        while (true) {
            try {
                Update update = queue.take();
                botAlgorithm.onUpdate(update);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public void setBotAlgorithm(BotAlgorithm botAlgorithm) {
        this.botAlgorithm = botAlgorithm;
    }

    public BotAlgorithm getBotAlgorithm() {
        return botAlgorithm;
    }

    public ArrayBlockingQueue<Update> getQueue() {
        return queue;
    }

    public void setQueue(ArrayBlockingQueue<Update> queue) {
        this.queue = queue;
    }
}
