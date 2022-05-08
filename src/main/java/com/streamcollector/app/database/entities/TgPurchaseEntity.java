package com.streamcollector.app.database.entities;

import jdk.jfr.Unsigned;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Cacheable
@Table(name = "tgpurchases")
public class TgPurchaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @Unsigned
    public Long id;

    @ManyToOne
    @JoinColumn(name="tgUser_id", nullable = false)
    public TgUserEntity tgUser;

    @Column(name = "amount", nullable = false)
    @Unsigned
    public Integer amount;

    @Column(name = "goodType", nullable = false)
    public String goodType;

    @Column(name = "applyTime", nullable = false)
    public ZonedDateTime applyTime;

    @Column(name = "durationEnd")
    public ZonedDateTime durationEnd;
}
