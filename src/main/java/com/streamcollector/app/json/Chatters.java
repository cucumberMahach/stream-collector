package com.streamcollector.app.json;

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

    public Chatters(){
        broadcaster = new String[0];
        vips = new String[0];
        moderators = new String[0];
        staff = new String[0];
        admins = new String[0];
        global_mods = new String[0];
        viewers = new String[0];
    }

    public void fillSetsFromArrays(){
        broadcaster_set.clear();
        vips_set.clear();
        moderators_set.clear();
        staff_set.clear();
        admins_set.clear();
        global_mods_set.clear();
        viewers_set.clear();

        if (broadcaster != null)
            broadcaster_set.addAll(Arrays.asList(broadcaster));
        if (vips != null)
            vips_set.addAll(Arrays.asList(vips));
        if (moderators != null)
            moderators_set.addAll(Arrays.asList(moderators));
        if (staff != null)
            staff_set.addAll(Arrays.asList(staff));
        if (admins != null)
            admins_set.addAll(Arrays.asList(admins));
        if (global_mods != null)
            global_mods_set.addAll(Arrays.asList(global_mods));
        if (viewers != null)
            viewers_set.addAll(Arrays.asList(viewers));
    }

    public boolean isEqual(Chatters other){
        /*return (Arrays.deepEquals(broadcaster, other.broadcaster) &&
                Arrays.deepEquals(vips,other.vips) &&
                Arrays.deepEquals(moderators,other.moderators) &&
                Arrays.deepEquals(staff,other.staff) &&
                Arrays.deepEquals(admins,other.admins) &&
                Arrays.deepEquals(global_mods,other.global_mods) &&
                Arrays.deepEquals(viewers,other.viewers)
        );*/
        return broadcaster_set.equals(other.broadcaster_set) &&
                vips_set.equals(other.vips_set) &&
                moderators_set.equals(other.moderators_set) &&
                staff_set.equals(other.staff_set) &&
                admins_set.equals(other.admins_set) &&
                global_mods_set.equals(other.global_mods_set) &&
                viewers_set.equals(other.viewers_set);
    }

    public String[] getByUserType(String type){
        return switch (type) {
            case "admin" -> admins;
            case "staff" -> staff;
            case "broadcaster" -> broadcaster;
            case "moderator" -> moderators;
            case "vip" -> vips;
            case "viewer" -> viewers;
            case "globalMod" -> global_mods;
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
            case "globalMod" -> global_mods_set;
            default -> null;
        };
    }
}


