package org.kendar.be.data;

import org.kendar.be.data.entities.Employee;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface EmployeeRepository extends MongoRepository<Employee, Long> {

    /*
    Employee getById(Long employeeId);*/
}