package com.example.appointmentservice.client;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.example.appointmentservice.dto.DoctorDto;

import io.github.resilience4j.retry.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@Slf4j
@Component
public class DoctorServiceClient {

    private final RestTemplate restTemplate;
    private final Retry doctorRetry;

    @Value("${services.doctor.base-url}")
    private String doctorServiceUrl;

    public DoctorServiceClient(RestTemplate restTemplate, @Qualifier("doctorServiceRetry") Retry doctorRetry) {
        this.restTemplate = restTemplate;
        this.doctorRetry = doctorRetry;
    }

    public DoctorDto getDoctorById(UUID id, String authHeader) {
        String url = doctorServiceUrl + "/api/doctors/" + id;
        log.debug("Calling Doctor service: {}", url);
        try {
            java.util.function.Supplier<DoctorDto> supplier =
                    io.github.resilience4j.retry.Retry.decorateSupplier(doctorRetry,
                            () -> {
                                HttpHeaders headers = new HttpHeaders();
                                if (authHeader != null && !authHeader.isBlank()) {
                                    headers.set("Authorization", authHeader);
                                }
                                HttpEntity<Void> entity = new HttpEntity<>(headers);
                                ResponseEntity<DoctorDto> response = restTemplate.exchange(url, HttpMethod.GET, entity, DoctorDto.class);
                                return response.getBody();
                            });
            return supplier.get();
        } catch (Exception e) {
            log.error("Error calling Doctor service for id {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to fetch doctor: " + e.getMessage(), e);
        }
    }

    // Fetch slot details (returns a map of slot fields)
    public Map<String, Object> getSlotById(UUID slotId, String authHeader) {
        try {
            String url = doctorServiceUrl + "/api/slots/" + slotId;
            HttpHeaders headers = new HttpHeaders();
            if (authHeader != null) headers.set("Authorization", authHeader);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> resp = restTemplate.postForEntity(url, entity, Map.class);
            // In some setups get is used; try getForObject as fallback
            return resp.getBody();
        } catch (Exception e) {
            log.error("Error fetching slot {}: {}", slotId, e.getMessage());
            throw new RuntimeException("Failed to fetch slot: " + e.getMessage(), e);
        }
    }

    // Reserve a slot: POST /api/slots/{slotId}/reserve with body { patientId, appointmentId }
    public Map<String, Object> reserveSlot(UUID slotId, String authHeader, UUID patientId, UUID appointmentId) {
        try {
            String url = doctorServiceUrl + "/api/slots/" + slotId + "/reserve";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (authHeader != null) headers.set("Authorization", authHeader);

            Map<String, Object> body = Map.of(
                "patientId", patientId,
                "appointmentId", appointmentId
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> resp = restTemplate.postForEntity(url, entity, Map.class);
            return resp.getBody();
        } catch (Exception e) {
            log.error("Error reserving slot {}: {}", slotId, e.getMessage());
            throw new RuntimeException("Failed to reserve slot: " + e.getMessage(), e);
        }
    }

    // Release a slot: POST /api/slots/{slotId}/release
    public Map<String, Object> releaseSlot(UUID slotId, String authHeader) {
        try {
            String url = doctorServiceUrl + "/api/slots/" + slotId + "/release";
            HttpHeaders headers = new HttpHeaders();
            if (authHeader != null) headers.set("Authorization", authHeader);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> resp = restTemplate.postForEntity(url, entity, Map.class);
            return resp.getBody();
        } catch (Exception e) {
            log.error("Error releasing slot {}: {}", slotId, e.getMessage());
            throw new RuntimeException("Failed to release slot: " + e.getMessage(), e);
        }
    }
}
