package com.twitchcollector.app.service;

import java.util.ArrayList;
import java.util.HashMap;

public class ServiceManager {
    public static final ServiceManager instance = new ServiceManager();
    public static final String[] allServices = {"circle","bot","admin"};

    private final HashMap<String, AbstractService> services = new HashMap<>();
    private final ArrayList<String> usedServices = new ArrayList<>();

    private ServiceManager(){ }

    public AbstractService createService(String name){
        switch (name){
            case "circle":
                return new CircleService();
            case "bot":
                return new BotService();
            case "admin":
                return new AdminService();
        }
        return null;
    }

    public String setServiceEnabled(String name, boolean value){
        return setServiceEnabled(name, value, false, false);
    }

    public String setServiceEnabled(String name, boolean value, boolean internalStop, boolean saveStop){
        for (String sname : allServices){
            if (sname.equals(name)){
                if (value) {
                    if (services.containsKey(sname)) {
                        return "Service '"+sname+"' already enabled";
                    } else {
                        AbstractService service = createService(name);
                        if (service == null){
                            return "Cannot create service. Unknown service '"+name+"'";
                        }else {
                            if (!service.isReusable && usedServices.contains(sname)){
                                return "Unable to start again a non-reusable service '" + sname + "'";
                            }else {
                                services.put(sname, service);
                                usedServices.add(sname);
                                service.startService();
                                return null;
                            }
                        }
                    }
                }else{
                    if (!services.containsKey(sname)) {
                        return "Service '"+sname+"' is not running";
                    }else{
                        if (services.get(sname).isStoppable) {
                            if (saveStop){
                                services.get(sname).saveStop();
                            }else {
                                if (!internalStop)
                                    services.get(sname).stopService();
                                services.remove(sname);
                            }
                            return null;
                        }else{
                            return "Cannot stop unstoppable service '" + sname + "'";
                        }
                    }
                }
            }
        }
        return "Unknown service '"+name+"'";
    }

    public String setLogEnabled(String serviceName, boolean value){
        for (String sname : allServices){
            if (sname.equals(serviceName)){
                if (services.containsKey(sname)) {
                    services.get(sname).setLogEnabled(value);
                    return null;
                }else{
                    return "Service '"+serviceName+"' is not running";
                }
            }
        }
        return "Unknown service '"+serviceName+"'";
    }

    public HashMap<String, AbstractService> getServices() {
        return services;
    }
}
