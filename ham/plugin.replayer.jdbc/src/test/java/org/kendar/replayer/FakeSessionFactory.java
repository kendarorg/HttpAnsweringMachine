package org.kendar.replayer;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.kendar.servers.db.EntityManagerFunction;
import org.kendar.servers.db.EntityManagerFunctionResult;
import org.kendar.servers.db.HibernateSessionFactory;

import java.util.Optional;

public class FakeSessionFactory implements HibernateSessionFactory {
    @Override
    public SessionFactory createSession() throws HibernateException {
        return null;
    }

    @Override
    public <T> T transactionalResult(EntityManagerFunctionResult function) throws Exception {
        return null;
    }

    @Override
    public <T> T queryResult(EntityManagerFunctionResult function) throws Exception {
        return null;
    }

    @Override
    public <T> Optional<T> querySingle(EntityManagerFunctionResult function) throws Exception {
        return Optional.empty();
    }

    @Override
    public void transactional(EntityManagerFunction function) throws Exception {

    }

    @Override
    public void query(EntityManagerFunction function) throws Exception {
        function.apply(null);
    }

    @Override
    public void setConfiguration(Configuration configuration) {

    }
}
