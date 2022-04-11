package com.twitchcollector.app.json;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

public class Chatters {
    @SerializedName("broadcaster")
    public String[] broadcaster;

    @SerializedName("vips")
    public String[] vips;

    @SerializedName("moderators")
    public String[] moderators;

    @SerializedName("staff")
    public String[] staff;

    @SerializedName("admins")
    public String[] admins;

    @SerializedName("global_mods")
    public String[] global_mods;

    @SerializedName("viewers")
    public String[] viewers;

    public boolean isEqual(Chatters other){
        return (Arrays.deepEquals(broadcaster, other.broadcaster) &&
                Arrays.deepEquals(vips,other.vips) &&
                Arrays.deepEquals(moderators,other.moderators) &&
                Arrays.deepEquals(staff,other.staff) &&
                Arrays.deepEquals(admins,other.admins) &&
                Arrays.deepEquals(global_mods,other.global_mods) &&
                Arrays.deepEquals(viewers,other.viewers)
        );
    }

    public String[] getByUserType(String type){
        return switch (type) {
            case "admin" -> admins;
            case "staff" -> staff;
            case "broadcaster" -> broadcaster;
            case "moderator" -> moderators;
            case "vip" -> vips;
            case "viewer" -> viewers;
            default -> null;
        };
    }
}


