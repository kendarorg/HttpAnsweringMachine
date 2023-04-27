package org.kendar.be.api.v1;


import org.kendar.be.data.CounterService;
import org.kendar.be.data.CountersRepository;
import org.kendar.be.data.EmployeeRepository;
import org.kendar.be.data.entities.Counter;
import org.kendar.be.data.entities.Employee;
import org.kendar.be.data.exceptions.ItemNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController()
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final EmployeeRepository repository;
    private CounterService countersRepository;

    EmployeeController(EmployeeRepository repository,
                       CounterService countersRepository) {
        this.repository = repository;
        this.countersRepository = countersRepository;
    }


    // Aggregate root
    // tag::get-aggregate-root[]
    @GetMapping(value ="",produces = "application/json")
    List<EmployeeDto> all() {
        return repository.findAll()
                .stream().map(app->EmployeeDto.convert(app)).
                collect(Collectors.toList());
    }
    // end::get-aggregate-root[]

    @PostMapping(value ="",produces = "application/json")
    EmployeeDto newEmployee(@RequestBody EmployeeDto newEmployee) {
        newEmployee.setId(countersRepository.getNextValue("employee"));
        var newEmployeeEntity = EmployeeDto.convert(newEmployee);
        return EmployeeDto.convert(repository.save(newEmployeeEntity));
    }

    // Single item

    @GetMapping(value ="/{id}",produces = "application/json")
    EmployeeDto one(@PathVariable Long id) {
        return EmployeeDto.convert(repository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(""+id)));
    }

    @PutMapping(value ="/{id}",produces = "application/json")
    EmployeeDto replaceEmployee(@RequestBody EmployeeDto newEmployee, @PathVariable Long id) {

        return repository.findById(id)
                .map(employee -> {
                    employee.setName(newEmployee.getName());
                    employee.setRole(newEmployee.getRole());
                    return EmployeeDto.convert(repository.save(employee));
                })
                .orElseGet(() -> {
                    newEmployee.setId(id);
                    return EmployeeDto.convert(repository.save(EmployeeDto.convert(newEmployee)));
                });
    }

    @DeleteMapping(value ="/{id}",produces = "application/json")
    void deleteEmployee(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
