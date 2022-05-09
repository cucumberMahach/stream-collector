package com.streamcollector.app.grabber;

import java.util.Objects;

public enum Platform {
    Twitch("twitch", "Twitch"),
    WASD("wasd", "WASD"),
    Trovo("trovo", "Trovo"),
    GoodGame("goodgame", "GoodGame");

    private final String nameInDB;
    private final String showName;

    Platform(String nameInDB, String showName){
        this.nameInDB = nameInDB;
        this.showName = showName;
    }

    public String getNameInDB() {
        return nameInDB;
    }

    public String getShowName() {
        return showName;
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
