package org.kendar.be.api.v1;

import org.kendar.be.data.AppointmentRepository;
import org.kendar.be.data.EmployeeRepository;
import org.kendar.be.data.entities.Appointment;
import org.kendar.be.data.entities.AppointmentId;
import org.kendar.be.data.entities.AppointmentStatus;
import org.kendar.be.data.exceptions.ItemNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController()
@RequestMapping("/api/v1/appointments")
public class AppointmentController {
    private final AppointmentRepository appointmentRepository;
    private EmployeeRepository employeeRepository;

    AppointmentController(AppointmentRepository appointmentRepository, EmployeeRepository employeeRepository) {
        this.appointmentRepository = appointmentRepository;
        this.employeeRepository = employeeRepository;
    }


    // Aggregate root
    // tag::get-aggregate-root[]
    @GetMapping(value ="",produces = "application/json")
    List<AppointmentDto> all() {
        return appointmentRepository.findAll()
                .stream().map(app->AppointmentDto.convert(app)).
                collect(Collectors.toList());
    }
    // end::get-aggregate-root[]

    @PostMapping(value ="/{employeeId}",produces = "application/json")
    AppointmentDto newAppointment(@RequestBody AppointmentDto newAppointment,@PathVariable Long employeeId) {
        var employee = employeeRepository.getById(employeeId);
        if(employee==null){
            throw new ItemNotFoundException(employeeId.toString());
        }
        newAppointment.setEmployeeId(employee.getId());
        var newAppointmentEntity = AppointmentDto.convert(newAppointment);
        return AppointmentDto.convert(appointmentRepository.save(newAppointmentEntity));
    }

    // Aggregate root
    // tag::get-aggregate-root[]
    @GetMapping(value ="/{employeeId}",produces = "application/json")
    List<AppointmentDto> allByEmployee(@PathVariable Long employeeId) {
        return appointmentRepository.findAll().stream().
                filter(app -> app.getEmployeeId()==employeeId).
                map(app->AppointmentDto.convert(app)).
                collect(Collectors.toList());
    }

    // Single item

    @GetMapping(value ="/{employeeId}/{appointmentId}",produces = "application/json")
    AppointmentDto one(@PathVariable Long employeeId,@PathVariable Long appointmentId) {
        var id = new AppointmentId(appointmentId,employeeId);
        return AppointmentDto.convert(appointmentRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id.toString())));
    }

    @PutMapping(value ="/{employeeId}/{appointmentId}",produces = "application/json")
    AppointmentDto replaceAppointment(@RequestBody AppointmentDto newAppointment, @PathVariable Long employeeId,@PathVariable Long appointmentId) {
        var id = new AppointmentId(appointmentId,employeeId);
        var newAppointmentEntity = AppointmentDto.convert(newAppointment);
        return AppointmentDto.convert(appointmentRepository.findById(id)
                .map(appointment -> {
                    appointment.setDate(newAppointment.getDate());
                    appointment.setDescription(newAppointment.getDescription());
                    appointment.setStatus(newAppointment.getStatus());
                    return appointmentRepository.save(appointment);
                })
                .orElseGet(() -> {
                    newAppointment.setId(appointmentId);
                    newAppointment.setEmployeeId(employeeId);
                    return appointmentRepository.save(newAppointmentEntity);
                }));
    }

    @DeleteMapping(value ="/{employeeId}/{appointmentId}",produces = "application/json")
    void deleteAppointment(@PathVariable Long employeeId,@PathVariable Long appointmentId) {
        var id = new AppointmentId(appointmentId,employeeId);
        appointmentRepository.deleteById(id);
    }

    @PutMapping(value ="/{employeeId}/{appointmentId}/state",produces = "application/json")
    Appointment changeState(@PathVariable Long employeeId,@PathVariable Long appointmentId) {
        var id = new AppointmentId(appointmentId,employeeId);
        return appointmentRepository.findById(id)
                .map(appointment -> {
                    if(appointment.getStatus()== AppointmentStatus.CREATED){
                        appointment.setStatus(AppointmentStatus.DRAFT);
                    }else if(appointment.getStatus()==AppointmentStatus.DRAFT){
                        appointment.setStatus(AppointmentStatus.CONFIRMED);
                    }
                    return appointmentRepository.save(appointment);
                })
                .orElseThrow(() -> {
                    throw new ItemNotFoundException(id.toString());
                });
    }
}
