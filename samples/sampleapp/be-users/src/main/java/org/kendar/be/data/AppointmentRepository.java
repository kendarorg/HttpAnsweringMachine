package org.kendar.be.data;


import org.kendar.be.data.entities.Appointment;
import org.kendar.be.data.entities.AppointmentId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, AppointmentId> {

}