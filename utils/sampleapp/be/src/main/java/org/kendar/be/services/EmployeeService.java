package org.kendar.be.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.kendar.be.data.entities.Employee;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Component
public class EmployeeService {
    @Value("${employee.location}")
    private String employeeLocation;
    private ObjectMapper mapper = new ObjectMapper();


    public List<Employee> findAll() throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        var request = new HttpGet(employeeLocation+"/api/v1/employees");
        var httpResponse = httpClient.execute(request);
        HttpEntity responseEntity = httpResponse.getEntity();
        InputStream in = responseEntity.getContent();
        return mapper.readValue(in, new TypeReference<>() {});
    }

    public Employee save(Employee newEmployee) {
        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            var request = new HttpPut(employeeLocation + "/api/v1/employees");
            var entity = new StringEntity(mapper.writeValueAsString(newEmployee));
            request.setEntity(entity);
            request.setHeader("content-type", "application/json");
            var httpResponse = httpClient.execute(request);
            HttpEntity responseEntity = httpResponse.getEntity();
            InputStream in = responseEntity.getContent();
            return mapper.readValue(in, new TypeReference<>() {
            });
        }catch (Exception ex){
            return null;
        }
    }

    public Optional<Employee> findById(Long id) throws IOException {
        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            var request = new HttpGet(employeeLocation + "/api/v1/employees/" + id);
            var httpResponse = httpClient.execute(request);
            HttpEntity responseEntity = httpResponse.getEntity();
            InputStream in = responseEntity.getContent();
            return Optional.of(mapper.readValue(in, Employee.class));
        }catch(Exception ex){
            return Optional.empty();
        }
    }

    public void deleteById(Long id) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        var request = new HttpDelete(employeeLocation+"/api/v1/employees/"+id);
        var httpResponse = httpClient.execute(request);
    }
}
