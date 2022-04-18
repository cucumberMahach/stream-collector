package com.twitchcollector.app.grabber;

import java.util.Objects;

public enum Platform {
    Twitch("twitch"),
    WASD("wasd"),
    Trovo("trovo");

    private final String nameInDB;

    Platform(String nameInDB){
        this.nameInDB = nameInDB;
    }

    public String getNameInDB() {
        return nameInDB;
    }

    public static Platform fromNameInDB(String nameInDB){
        var values = Platform.values();
        for (var v : values){
            if (Objects.equals(nameInDB, v.nameInDB)){
                return v;
            }
        }
        return null;
    }
}
