package com.twitchcollector.app.json;

import com.google.gson.annotations.SerializedName;

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
}


