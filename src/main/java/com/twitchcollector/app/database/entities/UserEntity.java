package com.twitchcollector.app.database.entities;

import jdk.jfr.Unsigned;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Cacheable
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @Unsigned
    public Long id;

    @Column(name = "name", unique = true, nullable = false)
    public String name;

    @ManyToOne
    @JoinColumn(name="site_id", nullable = false)
    public SiteEntity site;

    @OneToMany(mappedBy="user")
    public Set<UserChannelEntity> usersChannels = new HashSet<>();
}
