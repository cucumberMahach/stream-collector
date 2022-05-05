package com.streamcollector.app.grabber.goodgame.http;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class GGChannel {
    public Long id;

    public String key;

    public String premium;

    public String title;

    @SerializedName("max_viewers")
    public Long maxViewers;

    @SerializedName("player_type")
    public String playerType;

    @SerializedName("gg_player_src")
    public String ggPlayerSrc;

    public String embed;

    public String img;

    public String thumb;

    public String description;

    public Boolean adult;

    public Boolean hidden;

    public List<GGGame> games = new ArrayList<>();

    public String url;
}
