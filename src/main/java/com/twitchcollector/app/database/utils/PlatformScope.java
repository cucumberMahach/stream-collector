package com.twitchcollector.app.database.utils;

import com.twitchcollector.app.database.entities.SiteEntity;
import com.twitchcollector.app.grabber.Platform;
import org.hibernate.StatelessSession;

import java.util.List;

public class PlatformScope {

    private List<SiteEntity> entities;

    public void load(StatelessSession session){
        var query = session.createNativeQuery("select * from `twitch-collector`.sites", SiteEntity.class);
        entities = query.list();
    }

    public SiteEntity get(Platform platform){
        String name = platform.getNameInDB();
        return entities.stream().filter(siteEntity -> siteEntity.site.equals(name)).findFirst().get();
    }

    public List<SiteEntity> getAll(){
        return entities;
    }
}
