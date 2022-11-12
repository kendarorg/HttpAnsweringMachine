package org.kendar.servers.db;

import com.fasterxml.jackson.core.JsonProcessingException;

import javax.persistence.EntityManager;

@FunctionalInterface
public interface EntityManagerFunction {
    void apply(EntityManager em) throws Exception;
}
