package org.kendar.servers.db;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.stereotype.Component;

@Component
public class HibernateSessionFactoryImpl implements HibernateSessionFactory{
    private Configuration configuration;
    private static SessionFactory sessionFactory;

    @Override
    public SessionFactory createSession() throws HibernateException {

        if(sessionFactory==null) {
            sessionFactory = configuration.buildSessionFactory();
        }
        return sessionFactory;
    }

    @Override
    public void transactional(EntityManagerFunction function) throws Exception {
        var sessionFactory = createSession();
            var em = sessionFactory.createEntityManager();
            em.getTransaction().begin();
            function.apply(em);
            em.getTransaction().commit();
            em.close();

    }

    @Override
    public void query(EntityManagerFunction function) throws Exception {
        var sessionFactory = configuration.buildSessionFactory();
            var em = sessionFactory.createEntityManager();
            function.apply(em);
            em.close();
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
