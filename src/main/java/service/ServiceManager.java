package service;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class ServiceManager {
    public static final ServiceManager instance = new ServiceManager();
    public static final String[] allServices = {"circle","bot"};

    private final HashMap<String, AbstractService> services = new HashMap<>();

    private ServiceManager(){ }

    public AbstractService createService(String name){
        switch (name){
            case "circle":
                return new CircleService();
            case "bot":
                return new BotService();
        }
        return null;
    }

    public String setServiceEnabled(String name, boolean value){
        return setServiceEnabled(name, value, false);
    }

    public String setServiceEnabled(String name, boolean value, boolean internalStop){
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
                            services.put(sname, service);
                            service.startService();
                            return null;
                        }
                    }
                }else{
                    if (!services.containsKey(sname)) {
                        return "Service '"+sname+"' is not running";
                    }else{
                        if (!internalStop)
                            services.get(sname).stopService();
                        services.remove(sname);
                        return null;
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
