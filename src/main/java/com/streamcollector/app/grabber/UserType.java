package com.streamcollector.app.grabber;

public enum UserType {
    Admin("admin", "Администратор"),
    Staff("staff", "Стафф"),
    Broadcaster("broadcaster", "Стример"),
    Moderator("moderator", "Модератор"),
    Vip("vip", "Вип"),
    Viewer("viewer", "Зритель"),
    GlobalModerator("globalMod", "Глобальный модератор"),
    StreamModerator("stream_moder", "Модератор стрима"),
    SuperModerator("smoderator", "Супер-модератор"),
    Ace("ace", "Эйс"),
    AcePlus("aceplus", "Эйс плюс"),
    Creators("creators", "Создатель"),
    Editors("editors", "Редактор"),
    Followers("followers", "Фолловер"),
    Subscribers("subscribers", "Подписчик");

    public final String dbName;
    public final String rusName;

    UserType(String dbName, String rusName){
        this.dbName = dbName;
        this.rusName = rusName;
    }

    public static UserType fromDBName(String name){
        for (var val : UserType.values()){
            if (val.dbName.equals(name)){
                return val;
            }
        }
        return null;
    }
}
