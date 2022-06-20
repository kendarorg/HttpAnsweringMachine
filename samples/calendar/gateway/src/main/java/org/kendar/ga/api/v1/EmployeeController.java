package org.kendar.ga.api.v1;


import org.kendar.ga.data.exceptions.ItemNotFoundException;
import org.kendar.ga.services.Employee;
import org.kendar.ga.services.EmployeeService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController()
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeService service;

    EmployeeController(EmployeeService service) {
        this.service = service;
    }


    // Aggregate root
    // tag::get-aggregate-root[]
    @GetMapping(value ="",produces = "application/json")
    List<Employee> all() throws IOException {
        return service.findAll();
    }
    // end::get-aggregate-root[]

    @PostMapping(value ="",produces = "application/json")
    Employee newEmployee(@RequestBody Employee newEmployee) throws IOException {
        return service.save(newEmployee);
    }

    // Single item

    @GetMapping(value ="/{id}",produces = "application/json")
    Employee one(@PathVariable Long id) throws IOException {

        return service.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id.toString()));
    }

    @PutMapping(value ="/{id}",produces = "application/json")
    Employee replaceEmployee(@RequestBody Employee newEmployee, @PathVariable Long id) throws IOException {

        return service.replaceEmployee(newEmployee,id);
    }

    @DeleteMapping(value ="/{id}",produces = "application/json")
    void deleteEmployee(@PathVariable Long id) throws IOException {
        service.deleteById(id);
    }
}
