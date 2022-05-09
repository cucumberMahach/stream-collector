package com.streamcollector.app.tasks.task;

import com.streamcollector.app.bot.commands.LoadingMessage;

import java.util.HashMap;

public class Task {
    public TaskEvent onFinished;
    public LoadingMessage loadingMessage;
    public String chatId;
    public TaskType type = TaskType.None;
    public HashMap<String, Object> parameters = new HashMap<>();
    public HashMap<String, Object> results = new HashMap<>();
}
