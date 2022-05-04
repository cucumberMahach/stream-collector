package com.streamcollector.app.service;

import com.streamcollector.app.donations.StandardDonationsHandler;
import com.streamcollector.app.logging.LogStatus;
import com.streamcollector.app.settings.Settings;
import com.streamcollector.app.util.DataUtil;

public class DonationsService extends AbstractService{

    public DonationsService(){
        super("donations", true, true);
    }

    @Override
    protected void work() {
        var donations = new StandardDonationsHandler();
        donations.setService(this);
        donations.setPrintDebug(true);
        donations.setBearer(Settings.instance.getPrivateSettings().donatBearer);

        try {
            donations.start();
        }catch (Exception e){
            writeLog(LogStatus.Error, "Исключение при старте - " + DataUtil.getStackTrace(e));
        }

        try {
            while(true){
                Thread.sleep(10);
            }
        }catch (InterruptedException e){

        }

        try {
            donations.closeBlocking();
        } catch (InterruptedException e) {

        }
    }
}
