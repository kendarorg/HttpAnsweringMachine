package org.kendar.fe;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController()
@RequestMapping("/api/settings")
public class SettingsController {

    @Value("${employee.location}")
    private String employeeLocation;


    @Value("${appointment.location}")
    private String appointmentLocation;

    @GetMapping("")
    Map<String,String> all() {
        var result = new HashMap<String,String>();
        result.put("employee",employeeLocation);
        result.put("appointment",appointmentLocation);
        return result;
    }
}
