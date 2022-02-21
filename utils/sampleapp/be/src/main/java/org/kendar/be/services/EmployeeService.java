package org.kendar.be.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.kendar.be.data.entities.Employee;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class EmployeeService {
    @Value("${employee.location}")
    private String employeeLocation;
    private ObjectMapper mapper = new ObjectMapper();

    public Employee getById(Long employeeId) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        var request = new HttpGet(employeeLocation+"/api/v1/employees/"+employeeId);
        var httpResponse = httpClient.execute(request);
        HttpEntity responseEntity = httpResponse.getEntity();
        InputStream in = responseEntity.getContent();
        return mapper.readValue(in,Employee.class);
    }
}
