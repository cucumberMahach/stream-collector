package com.streamcollector.app.logging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Logger {

    public static final Logger instance = new Logger();
    public static final String GLOBAL_LOG_SERVICE_NAME = "global";
    public static final String LOG_FILENAME = "stream-collector.log";
    private static final Path LOG_FILENAME_PATH = Paths.get(LOG_FILENAME);

    private boolean globalLogEnabled = false;
    private final ArrayList<LogMessage> globalLogMessages = new ArrayList<>();

    private int maxLogMessages = 100;
    private int maxFileSizeKB = 115 * 1024;
    private int fileSizeToTrimKB = 100 * 1024;

    private Logger(){}

    public void writeLog(LogStatus status, String message){
        LogMessage msg = new LogMessage();
        msg.serviceName = GLOBAL_LOG_SERVICE_NAME;
        msg.message = message;
        msg.status = status;
        globalLogMessages.add(msg);
        if (globalLogEnabled)
            System.out.println(msg.getColorizedLine());

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
        try {
            if (!Files.exists(LOG_FILENAME_PATH))
                Files.createFile(LOG_FILENAME_PATH);
            Files.writeString(LOG_FILENAME_PATH, message.getLine() + '\n', StandardCharsets.UTF_8, StandardOpenOption.APPEND);
            constraintFile(LOG_FILENAME_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void constraintFile(Path file) throws IOException {
        final var size = Files.size(file);
        if (size > maxFileSizeKB * 1024L){
            final var lines = Files.readAllLines(file);
            List<String> newLines = new ArrayList<>();
            long newSize = 0;
            final long fileSizeToTrim = fileSizeToTrimKB * 1024L;
            for (int i = lines.size()-1; i >= 0; i--){
                var line = lines.get(i);
                final long lineSize = line.getBytes(StandardCharsets.UTF_8).length + 1L;
                if (newSize + lineSize > fileSizeToTrim) {
                    break;
                }
                newLines.add(line);
                newSize += lineSize;
            }

            Collections.reverse(newLines);
            Files.delete(file);
            Files.write(file, newLines, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
        }
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

    public int getMaxFileSizeKB() {
        return maxFileSizeKB;
    }

    public void setMaxFileSizeKB(int maxFileSizeKB) {
        this.maxFileSizeKB = maxFileSizeKB;
    }

    public int getFileSizeToTrimKB() {
        return fileSizeToTrimKB;
    }

    public void setFileSizeToTrimKB(int fileSizeToTrimKB) {
        this.fileSizeToTrimKB = fileSizeToTrimKB;
    }
}
