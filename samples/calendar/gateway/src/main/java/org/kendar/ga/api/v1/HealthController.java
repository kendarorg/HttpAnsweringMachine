package org.kendar.ga.api.v1;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/api/v1/health")
public class HealthController {
    @GetMapping(produces = "text/plain")
    String health() {

        return "OK";
    }
}
