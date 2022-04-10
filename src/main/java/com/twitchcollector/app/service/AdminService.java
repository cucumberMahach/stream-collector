package com.twitchcollector.app.service;

import com.twitchcollector.app.admin.stages.main.AdminApp;

public class AdminService extends AbstractService{

    public AdminService() {
        super("admin", true, false);
    }

    @Override
    protected void work() {
        try {
            AdminApp.startApp(this);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
