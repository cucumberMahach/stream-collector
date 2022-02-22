package logging;

import java.util.ArrayList;

public class Logger {

    public static final Logger instance = new Logger();
    public static final String GLOBAL_LOG_SERVICE_NAME = "global";

    private boolean globalLogEnabled = false;
    private final ArrayList<LogMessage> globalLogMessages = new ArrayList<>();

    private int maxLogMessages = 100;

    private Logger(){}

    public void writeLog(LogStatus status, String message){
        LogMessage msg = new LogMessage();
        msg.serviceName = GLOBAL_LOG_SERVICE_NAME;
        msg.message = message;
        msg.status = status;
        globalLogMessages.add(msg);
        if (globalLogEnabled)
            System.out.println(msg.getLine());

        constraintLogArray(globalLogMessages);
        processToFile(msg);
    }

    public void constraintLogArray(ArrayList<LogMessage> array){
        if (array.size() > maxLogMessages){
            int toRemove = array.size() - maxLogMessages;
            array.subList(0, toRemove).clear();
        }
    }

    public void processToFile(LogMessage message){
        //TODO
    }

    public boolean isGlobalLogEnabled() {
        return globalLogEnabled;
    }

    public void setGlobalLogEnabled(boolean globalLogEnabled) {
        this.globalLogEnabled = globalLogEnabled;
    }

    public int getMaxLogMessages() {
        return maxLogMessages;
    }

    public void setMaxLogMessages(int maxLogMessages) {
        this.maxLogMessages = maxLogMessages;
    }
}
