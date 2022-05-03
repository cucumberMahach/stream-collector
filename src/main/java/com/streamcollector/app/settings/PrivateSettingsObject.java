package com.streamcollector.app.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public class PrivateSettingsObject {

    @SerializedName("telegram_token")
    public String telegramToken;

    @SerializedName("trovo_client_id")
    public String trovoClientId;

    @SerializedName("donat_bearer")
    public String donatBearer;

    public String getString(){
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();
        return gson.toJson(this);
    }

    public boolean isSettingsCorrect(){
        return telegramToken != null && trovoClientId != null && donatBearer != null;
    }
}
