package com.streamcollector.app.tasks.database.results;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Duration;

@Entity
public class TopViewsByUserItem {
    @Id
    public Long id;

    @Column(name = "name")
    public String channelName;

    @Column(name = "channel_id")
    public Long channelId;

    @Column(name = "t_all")
    public Long durationSec;

    public Duration getDuration(){
        return Duration.ofSeconds(durationSec);
    }
}
