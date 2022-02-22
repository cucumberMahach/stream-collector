package service;

import logging.LogMessage;
import logging.LogStatus;
import logging.Logger;

import java.util.ArrayList;

public abstract class AbstractService extends Thread{

    private final ArrayList<LogMessage> logMessages = new ArrayList<>();
    protected String serviceName;
    private boolean logEnabled = false;

    public AbstractService(String serviceName){
        this.serviceName = serviceName;
    }

    protected abstract void work();

    protected void writeLog(LogStatus status, String message){
        LogMessage msg = new LogMessage();
        msg.serviceName = serviceName;
        msg.message = message;
        msg.status = status;
        logMessages.add(msg);
        if (logEnabled)
            System.out.println(msg.getLine());

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
        writeLog(LogStatus.Success, "Сервис запущен");
        work();
    }

    public void startService(){
        start();
    }

    public void stopService(){
        interrupt();
    }
}
