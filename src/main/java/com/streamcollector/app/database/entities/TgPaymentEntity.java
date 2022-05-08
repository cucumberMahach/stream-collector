package com.streamcollector.app.database.entities;

import jdk.jfr.Unsigned;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Cacheable
@Table(name = "tgpayments")
public class TgPaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @Unsigned
    public Long id;

    @ManyToOne
    @JoinColumn(name="tgUser_id", nullable = false)
    public TgUserEntity tgUser;

    @Column(name = "donationId")
    @Unsigned
    public Long donationId;

    @Column(name = "amount", nullable = false)
    @Unsigned
    public Integer amount;

    @Column(name = "donationTime")
    public ZonedDateTime donationTime;

    @Column(name = "getTime")
    public ZonedDateTime getTime;
}
