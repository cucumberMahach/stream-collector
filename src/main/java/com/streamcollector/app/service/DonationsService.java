package com.streamcollector.app.service;

import com.streamcollector.app.database.DatabaseUtil;
import com.streamcollector.app.donations.StandardDonationsHandler;
import com.streamcollector.app.logging.LogStatus;
import com.streamcollector.app.settings.Settings;
import com.streamcollector.app.util.DataUtil;
import org.hibernate.StatelessSession;

public class DonationsService extends AbstractService{

    private StatelessSession session;
    private long sessionTime = 0;

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
            return;
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

    private StatelessSession getSession(){
        var settings = Settings.instance.getSettings();
        return DatabaseUtil.getStateLessSession(settings.donationsDatabase);
    }

    public StatelessSession updateSession(){
        if (session == null){
            session = getSession();
            sessionTime = System.currentTimeMillis();
        }else{
            if (System.currentTimeMillis() - sessionTime > 5000){
                session.close();
                session = getSession();
                sessionTime = System.currentTimeMillis();
            }
        }
        return session;
    }
}
