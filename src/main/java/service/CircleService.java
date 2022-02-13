package service;

import logging.LogStatus;

public class CircleService extends AbstractService{

    public CircleService() {
        super("CircleService");
    }

    @Override
    protected void work() {
        while(true){
            writeLog(LogStatus.Warning, "Test warning message");
            try {
                sleep(1000);
            } catch (Exception e) {
                return;
            }
        }
    }
}
