import console.ConsoleProvider;
import logging.LogStatus;
import logging.Logger;

import java.util.logging.Level;

public class Main {
    public static void main(String[] args) {
        //java.util.logging.Logger.getLogger("org.hibernate").setLevel(Level.WARNING);
        Logger.instance.writeLog(LogStatus.Success, "Запуск");
        ConsoleProvider.instance.startConsole();
        Logger.instance.writeLog(LogStatus.Success, "Завершение работы");
        System.exit(0);
    }
}