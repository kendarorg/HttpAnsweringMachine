package org.kendar.be.api.v1;

import org.kendar.be.data.exceptions.ItemNotFoundException;
import org.kendar.be.services.Appointment;
import org.kendar.be.services.AppointmentService;
import org.kendar.be.services.AppointmentStatus;
import org.kendar.be.services.EmployeeService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController()
@RequestMapping("/api/v1/appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;
    private EmployeeService employeeService;

    AppointmentController(AppointmentService appointmentService, EmployeeService employeeService) {
        this.appointmentService = appointmentService;
        this.employeeService = employeeService;
    }


    // Aggregate root
    // tag::get-aggregate-root[]
    @GetMapping("")
    List<Appointment> all() throws IOException {
        return appointmentService.findAll();
    }
    // end::get-aggregate-root[]

    @PostMapping("/{employeeId}")
    Appointment newAppointment(@RequestBody Appointment newAppointment,@PathVariable Long employeeId) throws IOException {
        var employee = employeeService.findById(employeeId);
        if(employee.isEmpty()){
            throw new ItemNotFoundException(employeeId.toString());
        }
        newAppointment.setEmployeeId(employee.get().getId());
        return appointmentService.save(newAppointment);
    }

    // Aggregate root
    // tag::get-aggregate-root[]
    @GetMapping("/{employeeId}")
    List<Appointment> allByEmployee(@PathVariable Long employeeId) throws IOException {
        return appointmentService.findAll().stream().
                filter(app -> app.getEmployeeId()==employeeId).
                collect(Collectors.toList());
    }

    // Single item

    @GetMapping("/{employeeId}/{appointmentId}")
    Appointment one(@PathVariable Long employeeId,@PathVariable Long appointmentId) throws IOException {
        return appointmentService.findById(employeeId,appointmentId)
                .orElseThrow(() -> new ItemNotFoundException(employeeId.toString()+":"+appointmentId.toString()));
    }

    @PutMapping("/{employeeId}/{appointmentId}")
    Appointment replaceAppointment(@RequestBody Appointment newAppointment, @PathVariable Long employeeId,@PathVariable Long appointmentId) throws IOException {
            return appointmentService.findById(employeeId,appointmentId)
                .map(appointment -> {
                    appointment.setDate(newAppointment.getDate());
                    appointment.setDescription(newAppointment.getDescription());
                    return appointmentService.save(appointment);
                })
                .orElseGet(() -> {
                    newAppointment.setId(appointmentId);
                    newAppointment.setEmployeeId(employeeId);
                    return appointmentService.save(newAppointment);
                });
    }

    @DeleteMapping("/{employeeId}/{appointmentId}")
    void deleteAppointment(@PathVariable Long employeeId,@PathVariable Long appointmentId) throws IOException {
        appointmentService.deleteById(employeeId,appointmentId);
    }

    @PutMapping("/{employeeId}/{appointmentId}/state")
    Appointment changeState(@PathVariable Long employeeId,@PathVariable Long appointmentId) throws IOException {
        return appointmentService.findById(employeeId,appointmentId)
                .map(appointment -> {
                    if(appointment.getStatus()== AppointmentStatus.CREATED){
                        appointment.setStatus(AppointmentStatus.DRAFT);
                    }else if(appointment.getStatus()==AppointmentStatus.DRAFT){
                        appointment.setStatus(AppointmentStatus.CONFIRMED);
                    }
                    return appointmentService.save(appointment);
                })
                .orElseThrow(() -> {
                    throw new ItemNotFoundException(employeeId.toString()+":"+appointmentId.toString());
                });
    }
}
