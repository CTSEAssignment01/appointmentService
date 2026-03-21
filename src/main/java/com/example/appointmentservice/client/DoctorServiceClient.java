package com.example.appointmentservice.client;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.example.appointmentservice.dto.DoctorDto;

import io.github.resilience4j.retry.Retry;
import lombok.extern.slf4j.Slf4j;

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

    public DoctorDto getDoctorById(UUID id) {
        String url = doctorServiceUrl + "/api/doctors/" + id;
        log.debug("Calling Doctor service: {}", url);
        try {
            java.util.function.Supplier<DoctorDto> supplier =
                    io.github.resilience4j.retry.Retry.decorateSupplier(doctorRetry,
                            () -> restTemplate.getForObject(url, DoctorDto.class));
            return supplier.get();
        } catch (Exception e) {
            log.error("Error calling Doctor service for id {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to fetch doctor: " + e.getMessage(), e);
        }
    }
}
