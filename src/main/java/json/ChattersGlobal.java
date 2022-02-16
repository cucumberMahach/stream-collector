package json;

import com.google.gson.annotations.SerializedName;

public class ChattersGlobal {
    @SerializedName("chatter_count")
    public long chatterCount;

    @SerializedName("chatters")
    public Chatters chatters;

    public int calcUsersCount(){
        return chatters.viewers.length + chatters.moderators.length + chatters.admins.length + chatters.vips.length + chatters.broadcaster.length + chatters.staff.length;
    }
}
