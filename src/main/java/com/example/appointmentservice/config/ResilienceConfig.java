package com.example.appointmentservice.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

@Configuration
public class ResilienceConfig {

    @Bean("patientServiceRetry")
    public Retry patientServiceRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(500))
                .build();
        return Retry.of("patientService", config);
    }

    @Bean("doctorServiceRetry")
    public Retry doctorServiceRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(500))
                .build();
        return Retry.of("doctorService", config);
    }
}
