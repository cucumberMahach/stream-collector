package logging;

import static com.diogonunes.jcolor.Ansi.colorize;
import com.diogonunes.jcolor.Attribute;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class LogMessage {

    public Date dateTime = new Date();
    public String serviceName = "";
    public String message = "";
    public LogStatus status = LogStatus.None;

    public static final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    public String getLine(){
        String timestamp = formatter.format(dateTime);
        String colorized = "";
        switch (status){
            case Success -> {
                colorized = colorize("[SUCCESS]", Attribute.BRIGHT_GREEN_TEXT(), Attribute.BOLD());
            }
            case Warning -> {
                colorized = colorize("[WARNING]", Attribute.BRIGHT_YELLOW_TEXT(), Attribute.BOLD());
            }
            case Error -> {
                colorized = colorize("[ERROR]", Attribute.BRIGHT_RED_TEXT(), Attribute.BOLD());
            }
            case None -> {
                colorized = colorize("[-]", Attribute.BOLD());
            }
        }
        return timestamp + " " + colorized + " (" + serviceName + ") " + message;
    }
}
