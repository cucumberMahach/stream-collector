package com.streamcollector.app.database;

import com.streamcollector.app.database.entities.DonationEntity;
import com.streamcollector.app.database.entities.TgPaymentEntity;
import com.streamcollector.app.database.entities.TgUserEntity;
import org.hibernate.StatelessSession;

public class DonationsDatabase {
    public static void insertDonation(StatelessSession session, TgPaymentEntity payment){
        session.beginTransaction();
        session.insert(payment);
        session.getTransaction().commit();
    }

    public static DonationEntity getDonationEntity(StatelessSession session){
        var query = session.createNativeQuery("select * from `twitch-collector`.donations order by id", DonationEntity.class);
        query.setMaxResults(1);
        return query.uniqueResult();
    }

    public static void updateDonationEntity(StatelessSession session, DonationEntity entity){
        session.beginTransaction();
        session.update(entity);
        session.getTransaction().commit();
    }

    public static TgUserEntity getTgUserByDonationKey(StatelessSession session, String donationKey){
        var query = session.createNativeQuery("select * from `twitch-collector`.tgusers where donationKey = :key", TgUserEntity.class);
        query.setMaxResults(1);
        query.setParameter("key", donationKey);
        return query.uniqueResult();
    }

    public static void updateTgUser(StatelessSession session, TgUserEntity tgUser){
        session.beginTransaction();
        session.update(tgUser);
        session.getTransaction().commit();
    }
}
