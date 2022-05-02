package com.streamcollector.app.database.entities;

import jdk.jfr.Unsigned;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Cacheable
@Table(name = "circles")
public class CircleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @Unsigned
    public Long id;

    @Column(name = "number", nullable = false)
    @Unsigned
    public Long number;

    @Column(name = "startTime")
    public ZonedDateTime startTime;

    @Column(name = "endTime")
    public ZonedDateTime endTime;

    @Column(name = "totalChannels")
    public Integer totalChannels;

    @OneToMany(mappedBy="lastCircle")
    public Set<UserChannelEntity> usersChannelsLast = new HashSet<>();

    @OneToMany(mappedBy="firstCircle")
    public Set<UserChannelEntity> usersChannelsFirst = new HashSet<>();

    @OneToMany(mappedBy="lastCircle")
    public Set<ChannelEntity> channels = new HashSet<>();

    @OneToMany(mappedBy="circle")
    public Set<ChannelCircleEntity> channelsCircles = new HashSet<>();
}
