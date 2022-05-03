package com.streamcollector.app.donations.json.connect;

import com.google.gson.annotations.SerializedName;

public class OAuthUser {
    public Long id;

    public String code;

    public String name;

    public String avatar;

    public String email;

    public String language;

    @SerializedName("socket_connection_token")
    public String socketConnectionToken;
}
