package com.streamcollector.app.tasks;

import com.streamcollector.app.logging.LogStatus;
import com.streamcollector.app.tasks.task.Task;

import java.util.concurrent.ArrayBlockingQueue;

public class AsyncTaskExecutor extends Thread{

    protected TaskWorker taskWorker;
    protected ArrayBlockingQueue<Task> queue;
    protected TasksManager manager;

    public AsyncTaskExecutor(TasksManager service, String name) {
        super();
        this.manager = service;
        setName(name);
    }

    @Override
    public void run() {
        if (taskWorker == null){
            manager.writeLog(LogStatus.Error, String.format("AsyncTaskExecutor названный %s - не задан taskWorker", getName()));
            return;
        }

        if (queue == null){
            manager.writeLog(LogStatus.Error, String.format("AsyncTaskExecutor названный %s - не задана очередь заданий", getName()));
            return;
        }

        while (true) {
            try {
                Task task = queue.take();
                taskWorker.onNewTask(task);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    public ArrayBlockingQueue<Task> getQueue() {
        return queue;
    }

    public void setQueue(ArrayBlockingQueue<Task> queue) {
        this.queue = queue;
    }

    public TaskWorker getTaskWorker() {
        return taskWorker;
    }

    public void setTaskWorker(TaskWorker taskWorker) {
        this.taskWorker = taskWorker;
        taskWorker.setExecutor(this);
    }

    public TasksManager getManager() {
        return manager;
    }
}
