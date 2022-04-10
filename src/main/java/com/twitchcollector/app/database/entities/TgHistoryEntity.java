package com.twitchcollector.app.database.entities;

import jdk.jfr.Unsigned;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Cacheable
@Table(name = "tghistory")
public class TgHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @Unsigned
    public Long id;

    @ManyToOne
    @JoinColumn(name="tgUser_id", nullable = false)
    public TgUserEntity tgUser;

    @Column(name = "message", nullable = false)
    public String message;

    @Column(name = "result")
    public String result;

    @Column(name = "messageTime", nullable = false)
    public ZonedDateTime messageTime;

    @Column(name = "requestTime", nullable = false)
    public ZonedDateTime requestTime;

    @Column(name = "answerTime")
    public ZonedDateTime answerTime;
}
