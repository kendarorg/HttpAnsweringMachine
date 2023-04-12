package org.kendar.be.api.v1;


import org.kendar.be.data.CountersRepository;
import org.kendar.be.data.EmployeeRepository;
import org.kendar.be.data.entities.Counter;
import org.kendar.be.data.entities.Employee;
import org.kendar.be.data.exceptions.ItemNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeRepository repository;
    private CountersRepository countersRepository;

    EmployeeController(EmployeeRepository repository,
                       CountersRepository countersRepository) {
        this.repository = repository;
        this.countersRepository = countersRepository;
    }


    // Aggregate root
    // tag::get-aggregate-root[]
    @GetMapping(value ="",produces = "application/json")
    List<Employee> all() {
        return repository.findAll();
    }
    // end::get-aggregate-root[]

    @PostMapping(value ="",produces = "application/json")
    Employee newEmployee(@RequestBody Employee newEmployee) {
        var counter = countersRepository.findByCollection("employee");
        if(counter==null){
            counter = new Counter();
            counter.setTable("employee");
            counter.setCounter(1);
            countersRepository.save(counter);
        }else{
            counter.setCounter(counter.getCounter()+1);
            countersRepository.save(counter);
        }
        return repository.save(newEmployee);
    }

    // Single item

    @GetMapping(value ="/{id}",produces = "application/json")
    Employee one(@PathVariable Long id) {

        return repository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id.toString()));
    }

    @PutMapping(value ="/{id}",produces = "application/json")
    Employee replaceEmployee(@RequestBody Employee newEmployee, @PathVariable Long id) {

        return repository.findById(id)
                .map(employee -> {
                    employee.setName(newEmployee.getName());
                    employee.setRole(newEmployee.getRole());
                    return repository.save(employee);
                })
                .orElseGet(() -> {
                    newEmployee.setId(id);
                    return repository.save(newEmployee);
                });
    }

    @DeleteMapping(value ="/{id}",produces = "application/json")
    void deleteEmployee(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
