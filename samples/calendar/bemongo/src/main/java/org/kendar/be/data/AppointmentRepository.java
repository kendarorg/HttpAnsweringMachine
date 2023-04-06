package org.kendar.be.data;


import org.kendar.be.data.entities.Appointment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface AppointmentRepository extends MongoRepository<Appointment, Long> {

    @Query("{$and :[{employeeId:?1},{id:?0}]}")
    Optional<Appointment> findById(Long appointmentId, Long employeeId);

    @Query(value = "{$and :[{employeeId:?1},{id:?0}]}",delete = true)
    void deleteById(Long appointmentId, Long employeeId);
}