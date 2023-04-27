package org.kendar.be.api.v1;

import org.kendar.be.data.entities.Appointment;
import org.kendar.be.data.entities.AppointmentStatus;

import java.util.Calendar;

public class AppointmentDto {

    public static AppointmentDto convert(Appointment entity){
        var result = new AppointmentDto();
        result.setId(entity.getId());
        result.setEmployeeId(entity.getEmployeeId());
        result.setDate(entity.getDate());
        result.setStatus(AppointmentStatus.toEnum(entity.getStatus()));
        result.setDescription(entity.getDescription());
        return result;
    }


    public static Appointment convert(AppointmentDto entity) {
        var result = new Appointment();
        result.setId(entity.getId());
        result.setEmployeeId(entity.getEmployeeId());
        result.setDate(entity.getDate());
        result.setStatus(entity.getStatus().toString());
        result.setDescription(entity.getDescription());
        return result;
    }
    private Long id;
    private Long employeeId;

    private AppointmentStatus status;
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

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
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
}
