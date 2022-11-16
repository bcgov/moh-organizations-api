package ca.bc.hlth.mohorganizations;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping(value = "/health")
    String healthCheck() {
        return "Hello";
    }

}