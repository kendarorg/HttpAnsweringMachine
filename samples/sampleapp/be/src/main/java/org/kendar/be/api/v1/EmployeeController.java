package org.kendar.be.api.v1;


import org.kendar.be.data.entities.Employee;
import org.kendar.be.data.exceptions.ItemNotFoundException;
import org.kendar.be.services.EmployeeService;
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
    @GetMapping("")
    List<Employee> all() throws IOException {
        return service.findAll();
    }
    // end::get-aggregate-root[]

    @PostMapping("")
    Employee newEmployee(@RequestBody Employee newEmployee) throws IOException {
        return service.save(newEmployee);
    }

    // Single item

    @GetMapping("/{id}")
    Employee one(@PathVariable Long id) throws IOException {

        return service.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id.toString()));
    }

    @PutMapping("/{id}")
    Employee replaceEmployee(@RequestBody Employee newEmployee, @PathVariable Long id) throws IOException {

        return service.replaceEmployee(newEmployee,id);
    }

    @DeleteMapping("/{id}")
    void deleteEmployee(@PathVariable Long id) throws IOException {
        service.deleteById(id);
    }
}
