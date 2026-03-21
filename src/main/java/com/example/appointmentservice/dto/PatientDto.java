package com.example.appointmentservice.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class PatientDto {
    private UUID id;
    private UUID userId;
    private String name;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String gender;
    private String address;
    private String bloodGroup;
    private String allergies;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
