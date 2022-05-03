package com.streamcollector.app.donations.json.httpDonations;

import com.google.gson.annotations.SerializedName;

public class Meta {
    @SerializedName("current_page")
    public Long currentPage;

    public Long from;

    @SerializedName("last_page")
    public Long lastPage;

    public String path;

    @SerializedName("per_page")
    public Long perPage;

    public Long to;

    public Long total;
}
