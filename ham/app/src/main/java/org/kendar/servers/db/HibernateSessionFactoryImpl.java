package org.kendar.servers.db;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.stereotype.Component;

import javax.persistence.Query;
import java.util.List;
import java.util.Optional;

@Component
public class HibernateSessionFactoryImpl implements HibernateSessionFactory {
    private Configuration configuration;
    private static SessionFactory sessionFactory;

    private static final Object syncObject = new Object();

    @Override
    public SessionFactory createSession() throws HibernateException {
        if (sessionFactory != null) return sessionFactory;
        synchronized (syncObject) {
            if (sessionFactory == null) {
                sessionFactory = configuration.buildSessionFactory();
            }
        }
        return sessionFactory;
    }

    @Override
    public <T> T transactionalResult(EntityManagerFunctionResult function) throws Exception {
        var sessionFactory = createSession();
        var em = sessionFactory.createEntityManager();
        em.getTransaction().begin();
        T result = (T) function.apply(em);
        em.getTransaction().commit();
        em.close();
        return result;
    }

    @Override
    public <T> T queryResult(EntityManagerFunctionResult function) throws Exception {
        var sessionFactory = configuration.buildSessionFactory();
        var em = sessionFactory.createEntityManager();
        T result = (T) function.apply(em);
        em.close();
        return result;
    }

    @Override
    public <T> Optional<T> querySingle(EntityManagerFunctionResult function) throws Exception {
        var sessionFactory = configuration.buildSessionFactory();
        var em = sessionFactory.createEntityManager();
        var query = (Query) function.apply(em);
        Optional result;
        var list = (List<T>) query.getResultList();
        if (list.size() == 0) {
            em.close();
            result = Optional.empty();
        } else {
            result = Optional.of(list.get(0));
            em.close();
        }
        return result;
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
