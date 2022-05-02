package com.streamcollector.app.database.entities;

import jdk.jfr.Unsigned;

import javax.persistence.*;

@Entity
@Cacheable
@Table(name = "channelstocheck")
public class ChannelToCheckEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @Unsigned
    public Long id;

    @Column(name = "name", unique = true, nullable = false)
    public String name;

    @Column(name = "priority")
    public Integer priority;

    @ManyToOne
    @JoinColumn(name="site_id", nullable = false)
    public SiteEntity site;
}
