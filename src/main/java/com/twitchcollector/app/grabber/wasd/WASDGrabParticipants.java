package com.twitchcollector.app.grabber.wasd;

import java.util.ArrayList;

public class WASDGrabParticipants {
    public ArrayList<String> owners = new ArrayList<>();
    public ArrayList<String> moderators = new ArrayList<>();
    public ArrayList<String> users = new ArrayList<>();

    public ArrayList<Integer> addCountHistory = new ArrayList<>();

    public int countAll(){
        return owners.size() + moderators.size() + users.size();
    }

    public void add(WASDGrabParticipants other){
        owners.addAll(other.owners);
        moderators.addAll(other.moderators);
        users.addAll(other.users);
        addCountHistory.add(other.countAll());
    }
}
