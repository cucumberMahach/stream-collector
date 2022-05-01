package com.twitchcollector.app.grabber.trovo;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class TrovoGrabViewers {
    @SerializedName("live_title")
    public String listTitle;

    public String total;

    public String nickname;

    public Map<String, TrovoChattersViewers> chatters = new HashMap<>();

    @SerializedName("custome_roles")
    public Map<String, TrovoChattersViewers> customRoles = new HashMap<>();

    public transient ArrayList<Integer> addCountHistory = new ArrayList<>();

    public int countAll(){
        int count = 0;
        if (chatters.containsKey("all"))
            count = chatters.get("all").viewers.size();
        return count;
    }

    public void add(TrovoGrabViewers other){
        var keys = other.chatters.keySet();
        for (var key : keys){
            var others = other.chatters.get(key);
            if (chatters.containsKey(key)){
                var me = chatters.get(key);
                me.viewers.addAll(others.viewers);
            }else{
                chatters.put(key, others);
            }
        }

        keys = other.customRoles.keySet();
        for (var key : keys){
            var others = other.customRoles.get(key);
            if (customRoles.containsKey(key)){
                var me = customRoles.get(key);
                me.viewers.addAll(others.viewers);
            }else{
                customRoles.put(key, others);
            }
        }
        addCountHistory.add(other.countAll());
    }
}
