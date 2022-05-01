package com.twitchcollector.app.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.twitchcollector.app.logging.LogStatus;
import com.twitchcollector.app.logging.Logger;
import com.twitchcollector.app.util.DataUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Settings {
    public static final Settings instance = new Settings();

    private static final String STATIC_PRIVATE_FILE_NAME = "private/private.json";
    private static final String SETTINGS_FILE_NAME = "settings.json";
    private static final String STATIC_SETTINGS_FILE_NAME = "settings/settings.json";
    private SettingsObject settingsObject;
    private PrivateSettingsObject privateObject;
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
            result = settingsObject != null && settingsObject.isSettingsCorrect();
        if (!result && !staticSettings){
            customFileError = true;
            if (!load(true)){
                criticalError = true;
            }
        }else if (!result){
            criticalError = true;
        }

        if (!criticalError){
            boolean privateResult = loadPrivate();
            if (!privateResult || privateObject == null || !privateObject.isSettingsCorrect()){
                criticalError = true;
            }
        }
    }

    private boolean load(boolean staticSettings){
        try {
            settingsObject = new SettingsObject();
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

    private boolean loadPrivate() {
        try {
            privateObject = new PrivateSettingsObject();
            loadPrivateSettings();
            Logger.instance.writeLog(LogStatus.Success, "Приватные настройки загружены");
            return true;
        } catch (Throwable e) {
            Logger.instance.writeLog(LogStatus.Success, "Не удалось загрузить приватные настройки: " + DataUtil.getStackTrace(e));
            e.printStackTrace();
            return false;
        }
    }

    private void loadSettings(boolean staticSettings) throws IOException {
        String json = "";
        if (staticSettings){
            byte[] data;
            try(var stream = Settings.class.getClassLoader().getResourceAsStream(STATIC_SETTINGS_FILE_NAME)) {
                data = stream.readAllBytes();
            }
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

    private void loadPrivateSettings() throws IOException {
        String json = "";
        byte[] data;
        try(var stream = Settings.class.getClassLoader().getResourceAsStream(STATIC_PRIVATE_FILE_NAME)) {
            data = stream.readAllBytes();
        }
        json = new String(data, StandardCharsets.UTF_8);
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        privateObject = gson.fromJson(json, PrivateSettingsObject.class);
    }

    public SettingsObject getSettings(){
        return settingsObject;
    }

    public PrivateSettingsObject getPrivateSettings() {
        return privateObject;
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
