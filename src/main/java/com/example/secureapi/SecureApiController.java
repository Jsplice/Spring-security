package com.example.secureapi;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class SecureApiController {

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("message", "DevSecOps best practices are active");
        response.put("timestamp", Instant.now().toString());
        response.put("practices", Map.of(
            "sca", "OWASP Dependency-Check enabled",
            "sast", "SonarQube static analysis configured",
            "containerSecurity", "Non-root user, minimal JRE base image",
            "buildStrategy", "Multi-stage Docker build"
        ));
        return ResponseEntity.ok(response);
    }
}
