package org.kendar.servers.db;


import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public interface HibernateSessionFactory {
    SessionFactory createSession() throws HibernateException;
    void transactional(EntityManagerFunction function) throws Exception;

    void query(EntityManagerFunction function) throws Exception;

    void setConfiguration(Configuration configuration);
}
