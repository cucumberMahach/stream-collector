import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

class ChattersGlobal {
    @SerializedName("chatter_count")
    public long chatterCount;

    @SerializedName("chatters")
    public Chatters chatters;
}

class Chatters {
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


