package com.streamcollector.app.database.entities;

import jdk.jfr.Unsigned;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Cacheable
@Table(name = "sites")
public class SiteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @Unsigned
    public Long id;

    @Column(name = "site", unique = true, nullable = false)
    public String site;

    @OneToMany(mappedBy="site")
    public Set<ChannelToCheckEntity> channelsToCheck = new HashSet<>();

    @OneToMany(mappedBy="site")
    public Set<UserEntity> users = new HashSet<>();

    @OneToMany(mappedBy="site")
    public Set<ChannelEntity> channels = new HashSet<>();
}
