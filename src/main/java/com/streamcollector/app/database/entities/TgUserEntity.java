package com.streamcollector.app.database.entities;

import jdk.jfr.Unsigned;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Cacheable
@Table(name = "tgusers")
public class TgUserEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @Unsigned
    public Long id;

    @Column(name = "tg_id", unique = true, nullable = false)
    public String tgId;

    @Column(name = "firstName", nullable = false)
    public String firstName;

    @Column(name = "lastName")
    public String lastName;

    @Column(name = "username")
    public String username;

    @Column(name = "language", nullable = false)
    public String language;

    @Column(name = "messagesTotal", nullable = false)
    @Unsigned
    public Long messagesTotal = 0L;

    @Column(name = "state", nullable = false)
    public String state;

    @Column(name = "firstOnlineTime", nullable = false)
    public ZonedDateTime firstOnlineTime;

    @Column(name = "lastOnlineTime", nullable = false)
    public ZonedDateTime lastOnlineTime;

    @Column(name = "balance", nullable = false)
    public Long balance;

    @OneToMany(mappedBy="tgUser")
    public Set<TgHistoryEntity> tgHistory = new HashSet<>();

    @OneToMany(mappedBy="tgUser")
    public Set<TgBanEntity> tgBans = new HashSet<>();

    @OneToMany(mappedBy="tgUser")
    public Set<TgPurchaseEntity> tgPurchases = new HashSet<>();

    @OneToMany(mappedBy="tgUser")
    public Set<TgPaymentEntity> tgPayments = new HashSet<>();
}
