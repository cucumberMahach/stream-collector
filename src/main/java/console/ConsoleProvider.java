package console;

import com.diogonunes.jcolor.Attribute;
import service.ServiceManager;
import util.MavenData;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Scanner;
import java.util.stream.Collectors;

import static com.diogonunes.jcolor.Ansi.colorize;

public class ConsoleProvider {

    public static final ConsoleProvider instance = new ConsoleProvider();

    private Scanner scanner;

    private ConsoleProvider(){

    }

    public void startConsole(){
        ConsoleProvider.instance.printProgramHeader();
        scanner = new Scanner(System.in);
        System.out.println(colorize("Opening console...", Attribute.BRIGHT_GREEN_TEXT()));
        System.out.println(colorize("Enter 'help' for view all commands, 'exit' for quit from program", Attribute.BRIGHT_GREEN_TEXT()));
        String cmd = "";
        while (!cmd.equals("exit")){
            System.out.print("> ");
            cmd = scanner.nextLine().trim().toLowerCase(Locale.ROOT);
            //System.out.println(cmd);

            if (cmd.startsWith("start ")){
                String serviceName = cmd.substring("start ".length()).trim().toLowerCase(Locale.ROOT);
                String result = ServiceManager.instance.setServiceEnabled(serviceName, true);
                if (result == null){
                    System.out.print(colorize("Service '", Attribute.BRIGHT_GREEN_TEXT()));
                    System.out.print(colorize(serviceName, Attribute.BRIGHT_GREEN_TEXT(), Attribute.BOLD()));
                    System.out.println(colorize("' started", Attribute.BRIGHT_GREEN_TEXT()));
                }else{
                    System.out.print(colorize(result, Attribute.BRIGHT_RED_TEXT()));
                }
                continue;
            }

            if (cmd.startsWith("stop ")){
                String serviceName = cmd.substring("stop ".length()).trim().toLowerCase(Locale.ROOT);
                String result = ServiceManager.instance.setServiceEnabled(serviceName, false);
                if (result == null){
                    System.out.print(colorize("Service '", Attribute.BRIGHT_GREEN_TEXT()));
                    System.out.print(colorize(serviceName, Attribute.BRIGHT_GREEN_TEXT(), Attribute.BOLD()));
                    System.out.println(colorize("' stopped", Attribute.BRIGHT_GREEN_TEXT()));
                }else{
                    System.out.print(colorize(result, Attribute.BRIGHT_RED_TEXT()));
                }
                continue;
            }

            if (cmd.startsWith("log ")){
                String logName = cmd.substring("log ".length()).trim().toLowerCase(Locale.ROOT);
                String result = ServiceManager.instance.setLogEnabled(logName, true);
                if (result == null){
                    System.out.print(colorize("Log for service '", Attribute.BRIGHT_GREEN_TEXT()));
                    System.out.print(colorize(logName, Attribute.BRIGHT_GREEN_TEXT(), Attribute.BOLD()));
                    System.out.println(colorize("' opened. Press Enter to close", Attribute.BRIGHT_GREEN_TEXT()));

                    scanner.nextLine();

                    String resultOff = ServiceManager.instance.setLogEnabled(logName, false);
                    if (resultOff == null) {
                        System.out.print(colorize("Log for service '", Attribute.BRIGHT_GREEN_TEXT()));
                        System.out.print(colorize(logName, Attribute.BRIGHT_GREEN_TEXT(), Attribute.BOLD()));
                        System.out.println(colorize("' closed", Attribute.BRIGHT_GREEN_TEXT()));
                    }else{
                        System.out.print(colorize(resultOff, Attribute.BRIGHT_RED_TEXT()));
                    }
                }else{
                    System.out.print(colorize(result, Attribute.BRIGHT_RED_TEXT()));
                }
                continue;
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
                default:
                    System.out.print(colorize("Unknown command '", Attribute.BRIGHT_RED_TEXT()));
                    System.out.print(colorize(cmd, Attribute.BRIGHT_RED_TEXT(), Attribute.BOLD()));
                    System.out.println(colorize("'. Enter 'help' for view all commands", Attribute.BRIGHT_RED_TEXT()));
                    break;
            }
        }
        System.out.println(colorize("Closing console...", Attribute.BRIGHT_GREEN_TEXT()));
    }

    public void printProgramHeader(){
        System.out.print(colorize(MavenData.instance.getModel().getArtifactId().toUpperCase(Locale.ROOT), Attribute.BRIGHT_MAGENTA_TEXT()));
        System.out.print(colorize(" v" + MavenData.instance.getModel().getVersion(), Attribute.BRIGHT_YELLOW_TEXT()));
        System.out.println();
        System.out.println(colorize(MavenData.instance.getModel().getDescription(), Attribute.BRIGHT_CYAN_TEXT()));
    }

    private void printHelp(){
        System.out.println(colorize("Commands:", Attribute.BRIGHT_CYAN_TEXT()));
        System.out.println(colorize("\t exit - quit from program", Attribute.BRIGHT_CYAN_TEXT()));
        System.out.println(colorize("\t help - view all commands", Attribute.BRIGHT_CYAN_TEXT()));
        System.out.println(colorize("\t version - show information about program", Attribute.BRIGHT_CYAN_TEXT()));
        System.out.println(colorize("\t start [service name] - start service", Attribute.BRIGHT_CYAN_TEXT()));
        System.out.println(colorize("\t stop [service name] - stop service", Attribute.BRIGHT_CYAN_TEXT()));
        System.out.println(colorize("\t log [service name] - open log for service", Attribute.BRIGHT_CYAN_TEXT()));
        System.out.println(colorize("\t services - show all services names", Attribute.BRIGHT_CYAN_TEXT()));
        System.out.println(colorize("\t running - show all running services", Attribute.BRIGHT_CYAN_TEXT()));
    }
}
