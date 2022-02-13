package service;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class ServiceManager {
    public static final ServiceManager instance = new ServiceManager();
    public static final String[] allServices = {"circle"};

    private final HashMap<String, AbstractService> services = new HashMap<>();

    private ServiceManager(){ }

    public AbstractService createService(String name){
        switch (name){
            case "circle":
                return new CircleService();
        }
        return null;
    }

    public String setServiceEnabled(String name, boolean value){
        for (String sname : allServices){
            if (sname.equals(name)){
                if (value) {
                    if (services.containsKey(sname)) {
                        return "Service already enabled";
                    } else {
                        AbstractService service = createService(name);
                        if (service == null){
                            return "Cannot create service. Unknown service";
                        }else {
                            services.put(sname, service);
                            service.startService();
                            return null;
                        }
                    }
                }else{
                    if (!services.containsKey(sname)) {
                        return "Service is not running";
                    }else{
                        services.get(sname).stopService();
                        services.remove(sname);
                        return null;
                    }
                }
            }
        }
        return "Unknown service";
    }

    public String setLogEnabled(String serviceName, boolean value){
        for (String sname : allServices){
            if (sname.equals(serviceName)){
                if (services.containsKey(sname)) {
                    services.get(sname).setLogEnabled(value);
                    return null;
                }else{
                    return "Service is not running";
                }
            }
        }
        return "Unknown service";
    }

    public HashMap<String, AbstractService> getServices() {
        return services;
    }
}
