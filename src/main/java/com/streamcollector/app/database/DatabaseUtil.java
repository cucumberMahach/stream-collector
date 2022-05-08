package com.streamcollector.app.database;

import com.streamcollector.app.database.entities.*;
import com.streamcollector.app.logging.LogStatus;
import com.streamcollector.app.logging.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.cfg.Configuration;
import com.streamcollector.app.util.DataUtil;

public class DatabaseUtil {

    private static SessionFactory sessionFactory = null;

    private static void init(String configName){
        if (sessionFactory != null)
            return;
        try {
            Configuration configuration = new Configuration().configure(configName);

            configuration.addAnnotatedClass(ChannelEntity.class);
            configuration.addAnnotatedClass(ChannelToCheckEntity.class);
            configuration.addAnnotatedClass(CircleEntity.class);
            configuration.addAnnotatedClass(UserChannelEntity.class);
            configuration.addAnnotatedClass(UserEntity.class);
            configuration.addAnnotatedClass(UserTypeEntity.class);
            configuration.addAnnotatedClass(ChannelCircleEntity.class);
            configuration.addAnnotatedClass(SiteEntity.class);
            configuration.addAnnotatedClass(TgUserEntity.class);
            configuration.addAnnotatedClass(TgHistoryEntity.class);
            configuration.addAnnotatedClass(TgBanEntity.class);
            configuration.addAnnotatedClass(TgPurchaseEntity.class);
            configuration.addAnnotatedClass(TgPaymentEntity.class);
            configuration.addAnnotatedClass(DonationEntity.class);

            sessionFactory = configuration.buildSessionFactory();
        } catch (Throwable ex) {
            Logger.instance.writeLog(LogStatus.Error, "Ошибка инициализации БД: " + ex.getMessage() + " " + DataUtil.getStackTrace(ex));
            throw new ExceptionInInitializerError(ex);
        }
    }


    public static Session getSession(DatabaseConfigType databaseConfigType) throws HibernateException {
        init(databaseConfigType.getFileName());
        return sessionFactory.openSession();
    }


    public static StatelessSession getStateLessSession(DatabaseConfigType databaseConfigType) throws HibernateException{
        init(databaseConfigType.getFileName());
        return sessionFactory.openStatelessSession();
    }

    public static void shutdown() {
        sessionFactory.close();
    }
}