package com.twitchcollector.app.json;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.HashSet;

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

    public transient HashSet<String> broadcaster_set = new HashSet<>();
    public transient HashSet<String> vips_set = new HashSet<>();
    public transient HashSet<String> moderators_set = new HashSet<>();
    public transient HashSet<String> staff_set = new HashSet<>();
    public transient HashSet<String> admins_set = new HashSet<>();
    public transient HashSet<String> global_mods_set = new HashSet<>();
    public transient HashSet<String> viewers_set = new HashSet<>();

    public void fillSetsFromArrays(){
        broadcaster_set.clear();
        vips_set.clear();
        moderators_set.clear();
        staff_set.clear();
        admins_set.clear();
        global_mods_set.clear();
        viewers_set.clear();

        broadcaster_set.addAll(Arrays.asList(broadcaster));
        vips_set.addAll(Arrays.asList(vips));
        moderators_set.addAll(Arrays.asList(moderators));
        staff_set.addAll(Arrays.asList(staff));
        admins_set.addAll(Arrays.asList(admins));
        global_mods_set.addAll(Arrays.asList(global_mods));
        viewers_set.addAll(Arrays.asList(viewers));
    }

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

    public HashSet<String> getSetByUserType(String type){
        return switch (type) {
            case "admin" -> admins_set;
            case "staff" -> staff_set;
            case "broadcaster" -> broadcaster_set;
            case "moderator" -> moderators_set;
            case "vip" -> vips_set;
            case "viewer" -> viewers_set;
            default -> null;
        };
    }
}


