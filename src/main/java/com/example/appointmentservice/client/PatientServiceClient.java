package com.example.appointmentservice.client;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.example.appointmentservice.dto.PatientDto;

import io.github.resilience4j.retry.Retry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PatientServiceClient {

    private final RestTemplate restTemplate;
    private final Retry patientRetry;

    @Value("${services.patient.base-url}")
    private String patientServiceUrl;

    @Value("${services.user.base-url}")
    private String userServiceUrl;

    public PatientServiceClient(RestTemplate restTemplate, @Qualifier("patientServiceRetry") Retry patientRetry) {
        this.restTemplate = restTemplate;
        this.patientRetry = patientRetry;
    }

    public PatientDto getPatientById(UUID id, String authHeader) {
        try {
            String url = patientServiceUrl + "/api/patients/" + id;
            log.debug("Calling Patient service: {}", url);
            java.util.function.Supplier<PatientDto> supplier =
                    io.github.resilience4j.retry.Retry.decorateSupplier(patientRetry,
                            () -> {
                                HttpHeaders headers = new HttpHeaders();
                                if (authHeader != null && !authHeader.isBlank()) {
                                    headers.set("Authorization", authHeader);
                                }
                                HttpEntity<Void> entity = new HttpEntity<>(headers);
                                ResponseEntity<PatientDto> response = restTemplate.exchange(url, HttpMethod.GET, entity, PatientDto.class);
                                return response.getBody();
                            });
            return supplier.get();
        } catch (Exception e) {
            log.warn("Patient profile lookup failed for id {}: {}. Falling back to user-service validation.", id, e.getMessage());
            try {
                String userUrl = userServiceUrl + "/api/users/" + id;
                HttpHeaders headers = new HttpHeaders();
                if (authHeader != null && !authHeader.isBlank()) {
                    headers.set("Authorization", authHeader);
                }
                HttpEntity<Void> entity = new HttpEntity<>(headers);
                ResponseEntity<java.util.Map> response = restTemplate.exchange(userUrl, HttpMethod.GET, entity, java.util.Map.class);
                java.util.Map body = response.getBody();
                if (body == null) {
                    throw new RuntimeException("Empty user response");
                }

                PatientDto dto = new PatientDto();
                dto.setUserId(id);
                Object email = body.get("email");
                Object name = body.get("name");
                if (email != null) {
                    dto.setEmail(email.toString());
                }
                if (name != null) {
                    dto.setName(name.toString());
                }
                return dto;
            } catch (Exception userEx) {
                log.error("Error validating patient via user-service for id {}: {}", id, userEx.getMessage());
                throw new RuntimeException("Failed to fetch patient: " + userEx.getMessage(), userEx);
            }
        }
    }
}
