package com.streamcollector.app.tasks;

import com.streamcollector.app.logging.LogStatus;
import com.streamcollector.app.service.AbstractService;
import com.streamcollector.app.tasks.database.TaskDatabase;
import com.streamcollector.app.tasks.task.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class TasksManager {
    private final AbstractService service;
    private final ArrayBlockingQueue<Task> queue = new ArrayBlockingQueue<Task>(1, true);
    private final List<Task> finished = Collections.synchronizedList(new ArrayList<>());
    private final int threadsCount = 10;
    private final ArrayList<AsyncTaskExecutor> executors = new ArrayList<>();
    private boolean started = false;

    public TasksManager(AbstractService service){
        this.service = service;
    }

    public void start(){
        if (started)
            return;
        for (int thread = 0; thread < threadsCount; thread++){
            var executor = new AsyncTaskExecutor(this, String.format("TaskExecutor %d", thread+1));
            executor.setQueue(queue);
            executor.setTaskWorker(new TaskWorker(new TaskDatabase()));
            executors.add(executor);
            executor.start();
        }
        started = true;
    }

    public void stop(){
        if (!started)
            return;
        for (AsyncTaskExecutor executor : executors) {
            executor.interrupt();
        }
        executors.clear();
        started = false;
    }

    public synchronized void update(){
        for (Task task : finished) {
            task.onFinished.onEvent();
        }
        finished.clear();
    }

    public void executeTask(Task task){
        queue.add(task);
    }

    public synchronized void finishTask(Task task){
        finished.add(task);
    }

    public void writeLog(LogStatus status, String message){
        service.writeLog(status, message);
    }
}
