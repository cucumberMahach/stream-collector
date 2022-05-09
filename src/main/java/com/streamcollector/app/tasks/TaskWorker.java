package com.streamcollector.app.tasks;

import com.streamcollector.app.grabber.Platform;
import com.streamcollector.app.tasks.database.TaskDatabase;
import com.streamcollector.app.tasks.task.Task;
import com.streamcollector.app.util.TimeUtil;

public class TaskWorker {
    private AsyncTaskExecutor executor;
    private final TaskDatabase database;

    public TaskWorker(TaskDatabase database){
        this.database = database;
    }

    public void onNewTask(Task task){
        var username = (String) task.parameters.get("username");
        var platform = (Platform) task.parameters.get("platform");

        var now = TimeUtil.getZonedNow();
        var from = now.minusMonths(1);
        var to = now;

        var topViews = database.getTopViewsByUser(username, platform, from, to, 10);

        task.results.put("topViews", topViews);

        executor.getManager().finishTask(task);
    }

    public void setExecutor(AsyncTaskExecutor executor) {
        this.executor = executor;
    }
}

