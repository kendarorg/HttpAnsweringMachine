package org.kendar.be.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class AppointmentService {
    @Value("${appointment.location}")
    private String employeeLocation;
    private ObjectMapper mapper = new ObjectMapper();


    public List<Appointment> findAll() throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        var request = new HttpGet(employeeLocation+"/api/v1/appointments");
        var httpResponse = httpClient.execute(request);
        HttpEntity responseEntity = httpResponse.getEntity();
        var in = EntityUtils.toString(responseEntity, "UTF-8");
        return mapper.readValue(in, new TypeReference<>() {});
    }

    public Appointment save(Appointment newAppointment) {
        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            var request = new HttpPost(employeeLocation + "/api/v1/appointments/"+newAppointment.getEmployeeId());
            var entity = new StringEntity(mapper.writeValueAsString(newAppointment));
            request.setEntity(entity);
            request.setHeader("content-type", "application/json");
            var httpResponse = httpClient.execute(request);
            HttpEntity responseEntity = httpResponse.getEntity();
            var in = EntityUtils.toString(responseEntity, "UTF-8");
            return mapper.readValue(in, new TypeReference<>() {
            });
        }catch (Exception ex){
            return null;
        }
    }

    public Optional<Appointment> findById(Long employeeId,Long appointmentId) throws IOException {
        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            var request = new HttpGet(employeeLocation + "/api/v1/appointments/" + employeeId+"/"+appointmentId);
            var httpResponse = httpClient.execute(request);
            HttpEntity responseEntity = httpResponse.getEntity();
            var in = EntityUtils.toString(responseEntity, "UTF-8");
            return Optional.of(mapper.readValue(in, Appointment.class));
        }catch(Exception ex){
            return Optional.empty();
        }
    }

    public void deleteById(Long employeeId,Long appointmentId) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        var request = new HttpDelete(employeeLocation+"/api/v1/appointments/"+ employeeId+"/"+appointmentId);
        var httpResponse = httpClient.execute(request);
    }

    public Appointment replaceAppointment(Appointment newAppointment, Long employeeId,Long appointmentId) {
        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            var request = new HttpPut(employeeLocation + "/api/v1/appointments/"+ employeeId+"/"+appointmentId);
            var entity = new StringEntity(mapper.writeValueAsString(newAppointment));
            request.setEntity(entity);
            request.setHeader("content-type", "application/json");
            var httpResponse = httpClient.execute(request);
            HttpEntity responseEntity = httpResponse.getEntity();
            var in = EntityUtils.toString(responseEntity, "UTF-8");
            return mapper.readValue(in, new TypeReference<>() {
            });
        }catch (Exception ex){
            return null;
        }
    }
}
