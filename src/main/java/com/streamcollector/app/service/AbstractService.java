package com.streamcollector.app.service;

import com.streamcollector.app.logging.LogMessage;
import com.streamcollector.app.logging.LogStatus;
import com.streamcollector.app.logging.Logger;
import com.streamcollector.app.util.DataUtil;

import java.util.ArrayList;

public abstract class AbstractService extends Thread{

    private final ArrayList<LogMessage> logMessages = new ArrayList<>();
    protected String serviceName;
    protected boolean isStoppable;
    protected boolean isReusable;
    private boolean logEnabled = false;

    protected boolean running = false;

    public AbstractService(String serviceName){
        this(serviceName, true, true);
    }

    public AbstractService(String serviceName, boolean isStoppable, boolean isReusable){
        this.serviceName = serviceName;
        this.isStoppable = isStoppable;
        this.isReusable = isReusable;
    }

    protected abstract void work();

    public void writeLog(LogStatus status, String message){
        LogMessage msg = new LogMessage();
        msg.serviceName = serviceName;
        msg.message = message;
        msg.status = status;
        logMessages.add(msg);
        if (logEnabled)
            System.out.println(msg.getColorizedLine());

        Logger.instance.constraintLogArray(logMessages);
        Logger.instance.processToFile(msg);
    }

    public void setLogEnabled(boolean enabled){
        logEnabled = enabled;
    }

    public boolean isLogEnabled(){
        return logEnabled;
    }

    @Override
    public void run() {
        running = true;
        writeLog(LogStatus.Success, "Сервис запущен");
        try {
            work();
        }catch (Throwable ex){
            writeLog(LogStatus.Error, "Исключение в абстрактном сервисе: " + ex.getMessage() + " " + DataUtil.getStackTrace(ex));
            ex.printStackTrace();
        }
        if (!running){
            boolean globalLogEnabled = Logger.instance.isGlobalLogEnabled();
            Logger.instance.setGlobalLogEnabled(true);
            Logger.instance.writeLog(LogStatus.Success, String.format("Выполнено безопасное выключение сервиса %s", serviceName));
            Logger.instance.setGlobalLogEnabled(globalLogEnabled);
        }else {
            running = false;
        }
        writeLog(LogStatus.Success, "Сервис завершён");
        ServiceManager.instance.setServiceEnabled(serviceName, false);
    }

    public void startService(){
        start();
    }

    public void stopService(){
        interrupt();
    }

    public void saveStop(){
        running = false;
    }
}
