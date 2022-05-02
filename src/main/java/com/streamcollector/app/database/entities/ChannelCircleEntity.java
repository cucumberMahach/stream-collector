package com.streamcollector.app.database.entities;

import jdk.jfr.Unsigned;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Cacheable
@Table(name = "channels_circles")
public class ChannelCircleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @Unsigned
    public Long id;

    @ManyToOne
    @JoinColumn(name="circle_id")
    public CircleEntity circle;

    @ManyToOne
    @JoinColumn(name="channel_id")
    public ChannelEntity channel;

    @Column(name = "collectTime")
    public ZonedDateTime collectTime;

    @Column(name = "chattersCount")
    public Integer chattersCount;
}
