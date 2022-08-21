package com.streamcollector.app.remover;

import com.google.gson.annotations.SerializedName;
import com.streamcollector.app.database.DatabaseConfigType;

import java.time.ZonedDateTime;

public class RemoveObject {
    @SerializedName("next_remove_date")
    public ZonedDateTime nextRemoveDate;
}
