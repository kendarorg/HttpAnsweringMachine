package org.kendar.servers.db;

import javax.persistence.EntityManager;

@FunctionalInterface
public interface EntityManagerFunction {
    void apply(EntityManager em) throws Exception;
}
