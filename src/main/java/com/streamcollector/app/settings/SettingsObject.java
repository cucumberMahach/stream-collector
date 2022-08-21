package com.streamcollector.app.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.streamcollector.app.database.DatabaseConfigType;

public class SettingsObject {
    @SerializedName("circles_database")
    public DatabaseConfigType circlesDatabase;

    @SerializedName("bot_database")
    public DatabaseConfigType botDatabase;

    @SerializedName("bot_test_account")
    public Boolean botTestAccount;

    @SerializedName("admin_database")
    public DatabaseConfigType adminDatabase;

    @SerializedName("donations_database")
    public DatabaseConfigType donationsDatabase;

    @SerializedName("remover_enabled")
    public Boolean removerEnabled;

    @SerializedName("remover_optimize")
    public Boolean removerOptimize;

    public String getString(){
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();
        return gson.toJson(this);
    }

    public boolean isSettingsCorrect(){
        return circlesDatabase != null && botDatabase != null && adminDatabase != null && donationsDatabase != null && botTestAccount != null && removerEnabled != null && removerOptimize != null;
    }
}
