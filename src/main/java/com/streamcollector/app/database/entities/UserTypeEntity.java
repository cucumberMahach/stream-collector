package com.streamcollector.app.database.entities;

import jdk.jfr.Unsigned;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Cacheable
@Table(name = "user_types")
public class UserTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @Unsigned
    public Long id;

    @Column(name = "type", nullable = false)
    public String type;

    @OneToMany(mappedBy="type")
    public Set<UserChannelEntity> usersChannels = new HashSet<>();
}
