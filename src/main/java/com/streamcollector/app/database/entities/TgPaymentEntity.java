package com.streamcollector.app.database.entities;

import jdk.jfr.Unsigned;

import javax.persistence.*;

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
}
