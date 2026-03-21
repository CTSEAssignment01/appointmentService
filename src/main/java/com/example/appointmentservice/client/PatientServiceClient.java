package com.example.appointmentservice.client;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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

    public PatientServiceClient(RestTemplate restTemplate, @Qualifier("patientServiceRetry") Retry patientRetry) {
        this.restTemplate = restTemplate;
        this.patientRetry = patientRetry;
    }

    public PatientDto getPatientById(UUID id) {
        String url = patientServiceUrl + "/api/patients/" + id;
        log.debug("Calling Patient service: {}", url);
        try {
            java.util.function.Supplier<PatientDto> supplier =
                    io.github.resilience4j.retry.Retry.decorateSupplier(patientRetry,
                            () -> restTemplate.getForObject(url, PatientDto.class));
            return supplier.get();
        } catch (Exception e) {
            log.error("Error calling Patient service for id {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to fetch patient: " + e.getMessage(), e);
        }
    }
}
