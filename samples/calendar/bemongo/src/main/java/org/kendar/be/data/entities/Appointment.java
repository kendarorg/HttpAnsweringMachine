package org.kendar.be.data.entities;

import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonRepresentation;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Calendar;

@Document("appointment")
public class Appointment {
    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    @Id
    private String objectId;

    private Long id;

    private Long employeeId;


    private String status = AppointmentStatus.CREATED.toString();
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String state) {
        this.status = state;
    }
}
