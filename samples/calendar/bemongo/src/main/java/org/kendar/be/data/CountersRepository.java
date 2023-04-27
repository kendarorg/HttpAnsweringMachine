package org.kendar.be.data;

import org.kendar.be.data.entities.Counter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface CountersRepository extends MongoRepository<Counter,String> {
    @Query("{table:'?0'}")
    Counter findByCollection(String table);
}
