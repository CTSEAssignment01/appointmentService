package com.example.appointmentservice.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class DoctorDto {
    private UUID id;
    private UUID userId;
    private String name;
    private String specialization;
    private String email;
    private String phone;
    private String licenseNumber;
    private String department;
    private int yearsOfExperience;
    private boolean isActive;
    private boolean verified;
    private LocalDateTime createdAt;
    // Optional: slots may be included by doctor service when requested
    private List<Object> slots;
}
