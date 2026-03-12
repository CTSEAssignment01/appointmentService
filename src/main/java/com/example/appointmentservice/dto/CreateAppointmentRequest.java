package com.example.appointmentservice.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAppointmentRequest {

    @NotNull(message = "patientId is required")
    private UUID patientId;

    @NotNull(message = "doctorId is required")
    private UUID doctorId;

    @NotNull(message = "slotId is required")
    private UUID slotId;

    @NotBlank(message = "reason is required")
    private String reason;

    private String notes;
}