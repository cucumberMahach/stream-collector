package database;

import database.entities.*;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.cfg.Configuration;

public class DatabaseUtil {

    private static final SessionFactory sessionFactory;

    static {
        try {
            Configuration configuration = new Configuration().configure();

            configuration.addAnnotatedClass(ChannelEntity.class);
            configuration.addAnnotatedClass(ChannelToCheckEntity.class);
            configuration.addAnnotatedClass(CircleEntity.class);
            configuration.addAnnotatedClass(UserChannelEntity.class);
            configuration.addAnnotatedClass(UserEntity.class);

            sessionFactory = configuration.buildSessionFactory();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static Session getSession() throws HibernateException {
        return sessionFactory.openSession();
    }

    public static StatelessSession getStateLessSession() throws HibernateException{
        return sessionFactory.openStatelessSession();
    }

    public static void shutdown() {
        sessionFactory.close();
    }
}