package org.kendar.be.api.v1;

import org.kendar.be.data.entities.Employee;

public class EmployeeDto {

    public static EmployeeDto convert(Employee entity){
        var result = new EmployeeDto();
        result.setId(entity.getId());
        result.setName(entity.getName());
        result.setRole(entity.getRole());
        return result;
    }


    public static Employee convert(EmployeeDto entity) {
        var result = new Employee();
        result.setId(entity.getId());
        result.setName(entity.getName());
        result.setRole(entity.getRole());
        return result;
    }
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    private String name;
    private String role;

}
