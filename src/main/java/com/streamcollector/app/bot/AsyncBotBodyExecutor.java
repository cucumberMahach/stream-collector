package com.streamcollector.app.bot;

import com.streamcollector.app.logging.LogStatus;
import com.streamcollector.app.service.AbstractService;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.concurrent.ArrayBlockingQueue;

public class AsyncBotBodyExecutor extends Thread{

    protected BotStandardBody botBody;
    protected ArrayBlockingQueue<Update> queue;
    protected AbstractService service;

    public AsyncBotBodyExecutor(AbstractService service, String name) {
        super();
        this.service = service;
        setName(name);
    }

    @Override
    public void run() {
        if (botBody == null){
            service.writeLog(LogStatus.Error, String.format("AsyncBotBodyExecutor названный %s - не задан алгоритм бота", getName()));
            return;
        }

        if (queue == null){
            service.writeLog(LogStatus.Error, String.format("AsyncBotBodyExecutor названный %s - не задана очередь сообщений", getName()));
            return;
        }

        while (true) {
            try {
                Update update = queue.take();
                botBody.onUpdate(update);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    public void setBotBody(BotStandardBody botBody) {
        this.botBody = botBody;
    }

    public BotStandardBody getBotBody() {
        return botBody;
    }

    public ArrayBlockingQueue<Update> getQueue() {
        return queue;
    }

    public void setQueue(ArrayBlockingQueue<Update> queue) {
        this.queue = queue;
    }
}
