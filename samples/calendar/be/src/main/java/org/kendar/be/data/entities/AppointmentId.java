package org.kendar.be.data.entities;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.io.Serializable;

public class AppointmentId implements Serializable {
    @Id
    @GeneratedValue
    private Long id;

    @Override
    public String toString() {
        return "AppointmentId{" +
                "id=" + id +
                ", employeeId=" + employeeId +
                '}';
    }

    @Id
    private Long employeeId;

    public AppointmentId(){

    }

    public AppointmentId(Long id,Long employeeId){
        this.id = id;
        this.employeeId = employeeId;
    }
}
