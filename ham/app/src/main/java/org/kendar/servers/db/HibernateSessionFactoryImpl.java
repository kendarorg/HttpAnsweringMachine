package org.kendar.servers.db;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.stereotype.Component;

@Component
public class HibernateSessionFactoryImpl implements HibernateSessionFactory{
    private Configuration configuration;

    @Override
    public SessionFactory createSession() throws HibernateException {

            return configuration.buildSessionFactory();

    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
