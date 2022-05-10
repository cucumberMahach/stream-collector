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
        switch (task.type){
            case UserInfo -> doUserInfo(task);
            case UserSearch -> doUserSearch(task);
            case None -> executor.getManager().finishTask(task);
        }
    }

    private void doUserSearch(Task task){
        var username = (String) task.parameters.get("username");
        var users = database.searchUsers(username, 10, false);
        task.results.put("users", users);
        executor.getManager().finishTask(task);
    }

    private void doUserInfo(Task task){
        var username = (String) task.parameters.get("username");
        var platform = (Platform) task.parameters.get("platform");

        var user = database.getUser(username, platform);
        if (user == null){
            task.results.put("is_user_found", false);
        }else {
            task.results.put("is_user_found", true);

            var now = TimeUtil.getZonedNow();
            var from = now.minusMonths(1);
            var to = now;

            var topViews = database.getTopViewsByUser(username, platform, from, to, 10);

            task.results.put("top_views", topViews);
        }

        executor.getManager().finishTask(task);
    }

    public void setExecutor(AsyncTaskExecutor executor) {
        this.executor = executor;
    }
}

