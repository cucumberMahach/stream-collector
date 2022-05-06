package com.streamcollector.app.json;

import com.google.gson.annotations.SerializedName;
import com.streamcollector.app.grabber.UserType;

import java.util.*;

public class Chatters {
    @SerializedName("broadcaster")
    public final List<String> broadcaster = new ArrayList<>();

    @SerializedName("vips")
    public final List<String> vips = new ArrayList<>();

    @SerializedName("moderators")
    public final List<String> moderators = new ArrayList<>();

    @SerializedName("staff")
    public final List<String> staff = new ArrayList<>();

    @SerializedName("admins")
    public final List<String> admins = new ArrayList<>();

    @SerializedName("global_mods")
    public final List<String> global_mods = new ArrayList<>();

    @SerializedName("viewers")
    public final List<String> viewers = new ArrayList<>();

    public final List<String> stream_moder = new ArrayList<>();
    public final List<String> smoderator = new ArrayList<>();
    public final  List<String> ace = new ArrayList<>();
    public final List<String> aceplus = new ArrayList<>();
    public final List<String> creators = new ArrayList<>();
    public final List<String> editors = new ArrayList<>();
    public final List<String> followers = new ArrayList<>();
    public final List<String> subscribers = new ArrayList<>();

    public transient HashSet<String> broadcaster_set = new HashSet<>();
    public transient HashSet<String> vips_set = new HashSet<>();
    public transient HashSet<String> moderators_set = new HashSet<>();
    public transient HashSet<String> staff_set = new HashSet<>();
    public transient HashSet<String> admins_set = new HashSet<>();
    public transient HashSet<String> global_mods_set = new HashSet<>();
    public transient HashSet<String> viewers_set = new HashSet<>();

    public transient HashSet<String> stream_moder_set = new HashSet<>();
    public transient HashSet<String> smoderator_set = new HashSet<>();
    public transient HashSet<String> ace_set = new HashSet<>();
    public transient HashSet<String> aceplus_set = new HashSet<>();
    public transient HashSet<String> creators_set = new HashSet<>();
    public transient HashSet<String> editors_set = new HashSet<>();
    public transient HashSet<String> followers_set = new HashSet<>();
    public transient HashSet<String> subscribers_set = new HashSet<>();

    public transient HashMap<UserType, List<String>> arrMap = new HashMap<>();
    public transient HashMap<UserType, HashSet<String>> setMap = new HashMap<>();

    public Chatters(){
        constructMaps();
    }

    private void constructMaps(){
        arrMap.clear();
        setMap.clear();

        arrMap.put(UserType.Broadcaster, broadcaster);
        arrMap.put(UserType.Vip, vips);
        arrMap.put(UserType.Moderator, moderators);
        arrMap.put(UserType.Staff, staff);
        arrMap.put(UserType.Admin, admins);
        arrMap.put(UserType.GlobalModerator, global_mods);
        arrMap.put(UserType.Viewer, viewers);
        arrMap.put(UserType.StreamModerator, stream_moder);
        arrMap.put(UserType.SuperModerator, smoderator);
        arrMap.put(UserType.Ace, ace);
        arrMap.put(UserType.AcePlus, aceplus);
        arrMap.put(UserType.Creators, creators);
        arrMap.put(UserType.Editors, editors);
        arrMap.put(UserType.Followers, followers);
        arrMap.put(UserType.Subscribers, subscribers);

        setMap.put(UserType.Broadcaster, broadcaster_set);
        setMap.put(UserType.Vip, vips_set);
        setMap.put(UserType.Moderator, moderators_set);
        setMap.put(UserType.Staff, staff_set);
        setMap.put(UserType.Admin, admins_set);
        setMap.put(UserType.GlobalModerator, global_mods_set);
        setMap.put(UserType.Viewer, viewers_set);
        setMap.put(UserType.StreamModerator, stream_moder_set);
        setMap.put(UserType.SuperModerator, smoderator_set);
        setMap.put(UserType.Ace, ace_set);
        setMap.put(UserType.AcePlus, aceplus_set);
        setMap.put(UserType.Creators, creators_set);
        setMap.put(UserType.Editors, editors_set);
        setMap.put(UserType.Followers, followers_set);
        setMap.put(UserType.Subscribers, subscribers_set);
    }

    public void fillSetsAndConstructMaps(){
        for (var set : setMap.values()){
            set.clear();
        }

        var keys = UserType.values();
        for (var key : keys){
            var arr = arrMap.get(key);
            var set = setMap.get(key);
            set.addAll(arr);
        }

        /*if (broadcaster != null)
            broadcaster_set.addAll(Arrays.asList(broadcaster));
        if (vips != null)
            vips_set.addAll(Arrays.asList(vips));
        if (moderators != null)
            moderators_set.addAll(Arrays.asList(moderators));
        if (staff != null)
            staff_set.addAll(Arrays.asList(staff));
        if (admins != null)
            admins_set.addAll(Arrays.asList(admins));
        if (global_mods != null)
            global_mods_set.addAll(Arrays.asList(global_mods));
        if (viewers != null)
            viewers_set.addAll(Arrays.asList(viewers));

        if (stream_moder != null)
            stream_moder_set.addAll(Arrays.asList(stream_moder));
        if (smoderator != null)
            smoderator_set.addAll(Arrays.asList(smoderator));
        if (ace != null)
            ace_set.addAll(Arrays.asList(ace));
        if (aceplus != null)
            aceplus_set.addAll(Arrays.asList(aceplus));
        if (creators != null)
            creators_set.addAll(Arrays.asList(creators));
        if (editors != null)
            editors_set.addAll(Arrays.asList(editors));
        if (followers != null)
            followers_set.addAll(Arrays.asList(followers));
        if (subscribers != null)
            subscribers_set.addAll(Arrays.asList(subscribers));*/

        constructMaps();
    }

    public boolean isEqual(Chatters other){
        var keys = UserType.values();
        for (var key : keys){
            var set = setMap.get(key);
            var setOther = other.setMap.get(key);
            if (!set.equals(setOther))
                return false;
        }
        return true;

        /*return broadcaster_set.equals(other.broadcaster_set) &&
                vips_set.equals(other.vips_set) &&
                moderators_set.equals(other.moderators_set) &&
                staff_set.equals(other.staff_set) &&
                admins_set.equals(other.admins_set) &&
                global_mods_set.equals(other.global_mods_set) &&
                viewers_set.equals(other.viewers_set)
                &&
                stream_moder_set.equals(other.stream_moder_set) &&
                smoderator_set.equals(other.smoderator_set) &&
                ace_set.equals(other.ace_set) &&
                aceplus_set.equals(other.aceplus_set) &&
                creators_set.equals(other.creators_set) &&
                editors_set.equals(other.editors_set) &&
                followers_set.equals(other.followers_set) &&
                subscribers_set.equals(other.subscribers_set);*/
    }

    public List<String> getByUserType(String type){
        return arrMap.get(UserType.fromDBName(type));

            /*return switch (type) {
            case UserType.Admin.dbName. -> admins;
            case "staff" -> staff;
            case "broadcaster" -> broadcaster;
            case "moderator" -> moderators;
            case "vip" -> vips;
            case "viewer" -> viewers;
            case "globalMod" -> global_mods;
            default -> null;
        };*/
    }

    public HashSet<String> getSetByUserType(String type){
        return setMap.get(UserType.fromDBName(type));
        /*return switch (type) {
            case "admin" -> admins_set;
            case "staff" -> staff_set;
            case "broadcaster" -> broadcaster_set;
            case "moderator" -> moderators_set;
            case "vip" -> vips_set;
            case "viewer" -> viewers_set;
            case "globalMod" -> global_mods_set;
            default -> null;
        };*/
    }
}


