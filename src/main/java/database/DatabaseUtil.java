package database;

import database.entities.*;
import logging.LogStatus;
import logging.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.cfg.Configuration;
import util.DataUtil;

public class DatabaseUtil {

    private static SessionFactory sessionFactory = null;

    private static void init(){
        if (sessionFactory != null)
            return;
        try {
            Configuration configuration = new Configuration().configure();

            configuration.addAnnotatedClass(ChannelEntity.class);
            configuration.addAnnotatedClass(ChannelToCheckEntity.class);
            configuration.addAnnotatedClass(CircleEntity.class);
            configuration.addAnnotatedClass(UserChannelEntity.class);
            configuration.addAnnotatedClass(UserEntity.class);
            configuration.addAnnotatedClass(UserTypeEntity.class);
            configuration.addAnnotatedClass(ChannelCircleEntity.class);

            sessionFactory = configuration.buildSessionFactory();
        } catch (Throwable ex) {
            Logger.instance.writeLog(LogStatus.Error, "Ошибка инициализации БД: " + ex.getMessage() + " " + DataUtil.getStackTrace(ex));
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static Session getSession() throws HibernateException {
        init();
        return sessionFactory.openSession();
    }

    public static StatelessSession getStateLessSession() throws HibernateException{
        init();
        return sessionFactory.openStatelessSession();
    }

    public static void shutdown() {
        sessionFactory.close();
    }
}