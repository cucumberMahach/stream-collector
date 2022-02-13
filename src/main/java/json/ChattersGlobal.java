package json;

import com.google.gson.annotations.SerializedName;

public class ChattersGlobal {
    @SerializedName("chatter_count")
    public long chatterCount;

    @SerializedName("chatters")
    public Chatters chatters;
}
