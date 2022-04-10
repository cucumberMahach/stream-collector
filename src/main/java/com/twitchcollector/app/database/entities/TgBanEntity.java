package com.twitchcollector.app.database.entities;

import jdk.jfr.Unsigned;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Cacheable
@Table(name = "tgbans")
public class TgBanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @Unsigned
    public Long id;

    @ManyToOne
    @JoinColumn(name="tgUser_id", nullable = false)
    public TgUserEntity tgUser;

    @Column(name = "reason", nullable = false)
    public String reason;

    @Column(name = "fromTime", nullable = false)
    public ZonedDateTime fromTime;

    @Column(name = "untilTime", nullable = false)
    public ZonedDateTime untilTime;
}
