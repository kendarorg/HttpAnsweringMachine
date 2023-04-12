package org.kendar.be.data;

import org.kendar.be.data.entities.Employee;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface EmployeeRepository extends MongoRepository<Employee, String> {

    @Query("{id:?0}")
    Optional<Employee> findById(Long employeeId);

    @Query(value = "{id:?0}",delete = true)
    void deleteById(Long employeeId);
}