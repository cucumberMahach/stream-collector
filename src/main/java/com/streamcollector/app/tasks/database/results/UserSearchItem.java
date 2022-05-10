package com.streamcollector.app.tasks.database.results;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.ZonedDateTime;

@Entity
public class UserSearchItem {
    @Id
    public Long id;

    @Column(name = "user_name")
    public String userName;

    @Column(name = "site")
    public String site;

    @Column(name = "lastVisit")
    public ZonedDateTime lastVisit;

    @Column(name = "ch_name")
    public String channelName;

    @Column(name = "ch_id")
    public Long channelId;
}
