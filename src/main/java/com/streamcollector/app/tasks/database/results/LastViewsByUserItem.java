package com.streamcollector.app.tasks.database.results;

import com.streamcollector.app.grabber.UserType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Duration;
import java.time.ZonedDateTime;

@Entity
public class LastViewsByUserItem {
    @Id
    public Long id;

    @Column(name = "channelName")
    public String channelName;

    @Column(name = "sec")
    public Long durationAllTime;

    @Column(name = "time")
    public ZonedDateTime timestamp;

    @Column(name = "type")
    public String roleType;

    public Duration getDurationAllTime(){
        return Duration.ofSeconds(durationAllTime);
    }

    public UserType getUserTypeByRole(){
        return UserType.fromDBName(roleType);
    }
}
