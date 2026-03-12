package com.example.appointmentservice.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AppointmentResponse {

    private UUID id;
    private UUID patientId;
    private UUID doctorId;
    private UUID slotId;

    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private String status;
    private String reason;
    private String notes;

    private Instant createdAt;
    private Instant updatedAt;
}