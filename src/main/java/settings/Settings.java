package settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import logging.LogStatus;
import logging.Logger;
import util.DataUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Settings {
    public static final Settings instance = new Settings();

    private static final String SETTINGS_FILE_NAME = "settings.json";
    private static final String STATIC_SETTINGS_FILE_NAME = "settings/settings.json";
    private SettingsObject settingsObject;
    private boolean tryLoadFromCustomFile = false;
    private boolean criticalError = false;
    private boolean customFileError = false;

    private Settings() {
        var file = new File(SETTINGS_FILE_NAME);
        boolean staticSettings = !file.exists();
        if (!staticSettings){
            tryLoadFromCustomFile = true;
        }
        boolean result = load(staticSettings);
        if (result)
            result = settingsObject.isSettingsCorrect();
        if (!result && !staticSettings){
            customFileError = true;
            if (!load(true)){
                criticalError = true;
            }
        }else if (!result){
            criticalError = true;
        }
    }

    private boolean load(boolean staticSettings){
        try {
            loadSettings(staticSettings);
            if (!staticSettings){
                Logger.instance.writeLog(LogStatus.Success, "Загружены настройки из внешнего файла");
            }else{
                Logger.instance.writeLog(LogStatus.Success, "Загружены настройки из внутреннего файла");
            }
            return true;
        } catch (Throwable e) {
            if (!staticSettings){
                Logger.instance.writeLog(LogStatus.Error, "Не удалось загрузить настройки из внешнего файла: " + DataUtil.getStackTrace(e));
            }else{
                Logger.instance.writeLog(LogStatus.Error, "Не удалось загрузить настройки из внутреннего файла: " + DataUtil.getStackTrace(e));
            }
            e.printStackTrace();
            return false;
        }
    }

    private void loadSettings(boolean staticSettings) throws IOException {
        String json = "";
        if (staticSettings){
            var data = Settings.class.getClassLoader().getResourceAsStream(STATIC_SETTINGS_FILE_NAME).readAllBytes();
            json = new String(data, StandardCharsets.UTF_8);
        }else{
            try(var stream = new FileInputStream(SETTINGS_FILE_NAME)) {
                json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        settingsObject = gson.fromJson(json, SettingsObject.class);
    }

    public SettingsObject getSettings(){
        return settingsObject;
    }

    public boolean isTryLoadFromCustomFile() {
        return tryLoadFromCustomFile;
    }

    public boolean isCriticalError() {
        return criticalError;
    }

    public boolean isCustomFileError() {
        return customFileError;
    }
}
