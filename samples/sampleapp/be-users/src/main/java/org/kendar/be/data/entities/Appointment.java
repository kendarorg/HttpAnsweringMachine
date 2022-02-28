package org.kendar.be.data.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.util.Calendar;

@Entity
@IdClass(AppointmentId.class)
public class Appointment {
    @Id
    @GeneratedValue
    private Long id;
    @Id
    private Long employeeId;

    private AppointmentStatus status = AppointmentStatus.CREATED;
    private Calendar date;
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus state) {
        this.status = state;
    }
}
