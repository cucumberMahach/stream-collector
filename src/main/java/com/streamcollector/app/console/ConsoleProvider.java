package com.streamcollector.app.console;

import com.diogonunes.jcolor.Attribute;
import com.streamcollector.app.database.DatabaseUtil;
import com.streamcollector.app.logging.Logger;
import com.streamcollector.app.remover.Remover;
import com.streamcollector.app.service.ServiceManager;
import com.streamcollector.app.settings.Settings;
import com.streamcollector.app.util.Lambda;
import com.streamcollector.app.util.PropertiesFile;

import java.util.*;
import java.util.function.Function;

import static com.diogonunes.jcolor.Ansi.colorize;

public class ConsoleProvider {

    public static final ConsoleProvider instance = new ConsoleProvider();

    private Scanner scanner;
    private ArrayList<String> logsEnabled = new ArrayList<>();
    private boolean logsChanged = false;

    private ConsoleProvider(){

    }

    private void printSettingsStatus(){
        if (Settings.instance.isCriticalError()){
            System.out.println(colorize("*** Критическая ошибка при загрузке настроек ***", Attribute.BRIGHT_RED_TEXT(), Attribute.ITALIC()));
        }else{
            if (Settings.instance.isTryLoadFromCustomFile()){
                if (Settings.instance.isCustomFileError()){
                    System.out.println(colorize("*** Ошибка при загрузке настроек из внешнего файла ***", Attribute.BRIGHT_RED_TEXT(), Attribute.ITALIC()));
                    System.out.println(colorize("*** Настройки загружены из внутреннего файла ***", Attribute.BRIGHT_WHITE_TEXT(), Attribute.ITALIC()));
                }else{
                    System.out.println(colorize("*** Настройки загружены из внешнего файла ***", Attribute.BRIGHT_WHITE_TEXT(), Attribute.ITALIC()));
                }
            }else{
                System.out.println(colorize("*** Настройки загружены из внутреннего файла ***", Attribute.BRIGHT_WHITE_TEXT(), Attribute.ITALIC()));
            }
        }
    }

    private void privatePrivateSettingsStatus(){
        if (Settings.instance.isCriticalError()) {
            System.out.println(colorize("*** Критическая ошибка при загрузке настроек ***", Attribute.BRIGHT_RED_TEXT(), Attribute.ITALIC()));
        }else{
            System.out.println(colorize("*** Приватные настройки загружены ***", Attribute.BRIGHT_WHITE_TEXT(), Attribute.ITALIC()));
        }
    }

    public void startConsole(){
        ConsoleProvider.instance.printProgramHeader();
        scanner = new Scanner(System.in);
        printSettingsStatus();
        System.out.println(colorize("Opening console...", Attribute.BRIGHT_GREEN_TEXT()));
        System.out.println(colorize("Enter 'help' for view all commands, 'exit' for quit from program", Attribute.BRIGHT_GREEN_TEXT()));
        String cmd = "";
        while (!cmd.equals("exit")){
            System.out.print("> ");
            cmd = scanner.nextLine().trim().toLowerCase(Locale.ROOT);

            var commands = cmd.split("\\|");
            for (var i : commands){
                var prepI = i.trim();
                parseCmd(prepI);
            }
            // Logs
            if (logsChanged){
                logsChanged = false;
                scanner.nextLine();

                for (var logName : logsEnabled){
                    String resultOff = null;

                    if (logName.equals(Logger.GLOBAL_LOG_SERVICE_NAME))
                        Logger.instance.setGlobalLogEnabled(false);
                    else
                        resultOff = ServiceManager.instance.setLogEnabled(logName, false);

                    if (resultOff == null) {
                        System.out.print(colorize("Log for '", Attribute.BRIGHT_GREEN_TEXT()));
                        System.out.print(colorize(logName, Attribute.BRIGHT_GREEN_TEXT(), Attribute.BOLD()));
                        System.out.println(colorize("' closed", Attribute.BRIGHT_GREEN_TEXT()));
                    }else{
                        System.out.println(colorize(resultOff, Attribute.BRIGHT_RED_TEXT()));
                    }
                }

                logsEnabled.clear();
            }
        }
        System.out.println(colorize("Closing console...", Attribute.BRIGHT_GREEN_TEXT()));
    }

    private boolean parseCmd(String cmd){
        if (cmd.isBlank())
            return false;

        if (cmd.startsWith("start ")){
            String serviceName = cmd.substring("start ".length()).trim().toLowerCase(Locale.ROOT);
            String result = ServiceManager.instance.setServiceEnabled(serviceName, true);
            if (result == null){
                System.out.print(colorize("Service '", Attribute.BRIGHT_GREEN_TEXT()));
                System.out.print(colorize(serviceName, Attribute.BRIGHT_GREEN_TEXT(), Attribute.BOLD()));
                System.out.println(colorize("' started", Attribute.BRIGHT_GREEN_TEXT()));
            }else{
                System.out.println(colorize(result, Attribute.BRIGHT_RED_TEXT()));
            }
            return true;
        }

        if (cmd.startsWith("stop ")){
            String serviceName = cmd.substring("stop ".length()).trim().toLowerCase(Locale.ROOT);
            String result = ServiceManager.instance.setServiceEnabled(serviceName, false);
            if (result == null){
                System.out.print(colorize("Service '", Attribute.BRIGHT_GREEN_TEXT()));
                System.out.print(colorize(serviceName, Attribute.BRIGHT_GREEN_TEXT(), Attribute.BOLD()));
                System.out.println(colorize("' stopped", Attribute.BRIGHT_GREEN_TEXT()));
            }else{
                System.out.println(colorize(result, Attribute.BRIGHT_RED_TEXT()));
            }
            return true;
        }

        if (cmd.startsWith("savestop ")){
            String serviceName = cmd.substring("savestop ".length()).trim().toLowerCase(Locale.ROOT);
            String result = ServiceManager.instance.setServiceEnabled(serviceName, false, false, true);
            if (result == null){
                System.out.print(colorize("Save stop to service '", Attribute.BRIGHT_GREEN_TEXT()));
                System.out.print(colorize(serviceName, Attribute.BRIGHT_GREEN_TEXT(), Attribute.BOLD()));
                System.out.println(colorize("' executed", Attribute.BRIGHT_GREEN_TEXT()));
            }else{
                System.out.println(colorize(result, Attribute.BRIGHT_RED_TEXT()));
            }
            return true;
        }

        if (cmd.startsWith("log ")){
            String logName = cmd.substring("log ".length()).trim().toLowerCase(Locale.ROOT);
            String result = null;

            if (logName.equals(Logger.GLOBAL_LOG_SERVICE_NAME))
                Logger.instance.setGlobalLogEnabled(true);
            else
                result = ServiceManager.instance.setLogEnabled(logName, true);

            if (result == null){
                System.out.print(colorize("Log for '", Attribute.BRIGHT_GREEN_TEXT()));
                System.out.print(colorize(logName, Attribute.BRIGHT_GREEN_TEXT(), Attribute.BOLD()));
                System.out.println(colorize("' opened. Press Enter to close", Attribute.BRIGHT_GREEN_TEXT()));

                logsEnabled.add(logName);
                logsChanged = true;
            }else{
                System.out.println(colorize(result, Attribute.BRIGHT_RED_TEXT()));
            }
            return true;
        }

        if (cmd.startsWith("exec ")){
            String execName = cmd.substring("exec ".length()).trim().toLowerCase(Locale.ROOT);

            Lambda printSuccess  = () -> {
                System.out.print(colorize("Scenario for '", Attribute.BRIGHT_GREEN_TEXT()));
                System.out.print(colorize(execName, Attribute.BRIGHT_GREEN_TEXT(), Attribute.BOLD()));
                System.out.println(colorize("' executed", Attribute.BRIGHT_GREEN_TEXT()));
            };

            switch (execName){
                case "remover":
                    printSuccess.execute();
                    forceRemover();
                    break;
                default:
                    System.out.print(colorize("Unknown scenario '", Attribute.BRIGHT_RED_TEXT()));
                    System.out.print(colorize(execName, Attribute.BRIGHT_RED_TEXT(), Attribute.BOLD()));
                    System.out.println(colorize("'. Enter 'help' for view all commands", Attribute.BRIGHT_RED_TEXT()));
                    break;
            }

            return true;
        }

        switch (cmd){
            case "help":
                printHelp();
                break;
            case "version":
                printProgramHeader();
                break;
            case "services":
                System.out.println("All services: " + colorize(String.join(" ", ServiceManager.allServices), Attribute.BRIGHT_CYAN_TEXT(), Attribute.BOLD()));
                break;
            case "running":
                if (ServiceManager.instance.getServices().isEmpty())
                    System.out.println("No service is running");
                else
                    System.out.println("Running services: " + colorize(String.join(" ", ServiceManager.instance.getServices().keySet()), Attribute.BRIGHT_CYAN_TEXT(), Attribute.BOLD()));
                break;
            case "exit":
                break;
            case "settings":
                System.out.println("-----------------------------------------------");
                printSettingsStatus();
                System.out.println("-----------------------------------------------");
                System.out.println(Settings.instance.getSettings().getString());
                System.out.println("-----------------------------------------------");
                break;
            case "private":
                System.out.println("-----------------------------------------------");
                privatePrivateSettingsStatus();
                System.out.println("-----------------------------------------------");
                System.out.println(Settings.instance.getPrivateSettings().getString());
                System.out.println("-----------------------------------------------");
                break;
            default:
                System.out.print(colorize("Unknown command '", Attribute.BRIGHT_RED_TEXT()));
                System.out.print(colorize(cmd, Attribute.BRIGHT_RED_TEXT(), Attribute.BOLD()));
                System.out.println(colorize("'. Enter 'help' for view all commands", Attribute.BRIGHT_RED_TEXT()));
                break;
        }
        return false;
    }

    private void forceRemover(){
        var settings = Settings.instance.getSettings();
        var session = DatabaseUtil.getStateLessSession(settings.circlesDatabase);
        var remover = new Remover(null);
        remover.forced(session);
        session.close();
    }

    public void printProgramHeader(){
        System.out.print(colorize(PropertiesFile.instance.getArtifactId().toUpperCase(Locale.ROOT), Attribute.BRIGHT_MAGENTA_TEXT()));
        System.out.print(colorize(" v" + PropertiesFile.instance.getVersion(), Attribute.BRIGHT_YELLOW_TEXT()));
        System.out.println();
        System.out.println(colorize(PropertiesFile.instance.getDescription(), Attribute.BRIGHT_CYAN_TEXT()));
    }

    private void printHelp(){
        System.out.println(colorize("Commands:", Attribute.BRIGHT_CYAN_TEXT()));
        System.out.println(colorize("\t exit                      - quit from program", Attribute.BRIGHT_CYAN_TEXT()));
        System.out.println(colorize("\t help                      - view all commands", Attribute.BRIGHT_CYAN_TEXT()));
        System.out.println(colorize("\t version                   - show information about program", Attribute.BRIGHT_CYAN_TEXT()));
        System.out.println(colorize("\t start     [service name]  - start service", Attribute.BRIGHT_CYAN_TEXT()));
        System.out.println(colorize("\t stop      [service name]  - stop service", Attribute.BRIGHT_CYAN_TEXT()));
        System.out.println(colorize("\t savestop  [service name]  - safe service stop", Attribute.BRIGHT_CYAN_TEXT()));
        System.out.println(colorize("\t log       [service name]  - open log for service", Attribute.BRIGHT_CYAN_TEXT()));
        System.out.println(colorize("\t exec      [remover|...]   - exec scenario", Attribute.BRIGHT_CYAN_TEXT()));
        System.out.println(colorize("\t services                  - show all services names", Attribute.BRIGHT_CYAN_TEXT()));
        System.out.println(colorize("\t running                   - show all running services", Attribute.BRIGHT_CYAN_TEXT()));
        System.out.println(colorize("\t settings                  - show current settings", Attribute.BRIGHT_CYAN_TEXT()));
        System.out.println(colorize("\t private                   - show current private settings", Attribute.BRIGHT_CYAN_TEXT()));
    }
}
