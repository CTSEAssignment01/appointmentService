package com.example.appointmentservice.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.appointmentservice.dto.AppointmentResponse;
import com.example.appointmentservice.dto.CreateAppointmentRequest;
import com.example.appointmentservice.exception.BadRequestException;
import com.example.appointmentservice.exception.ResourceNotFoundException;
import com.example.appointmentservice.model.Appointment;
import com.example.appointmentservice.model.AppointmentStatus;
import com.example.appointmentservice.repository.AppointmentRepository;
import com.example.appointmentservice.util.AppointmentMapper;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {
        validateCreateRequest(request);

        if (appointmentRepository.existsBySlotIdAndStatus(request.getSlotId(), AppointmentStatus.BOOKED)) {
            throw new BadRequestException("This slot is already booked");
        }

        Appointment appointment = new Appointment();
        appointment.setPatientId(request.getPatientId());
        appointment.setDoctorId(request.getDoctorId());
        appointment.setSlotId(request.getSlotId());

        // Temporary values until doctorService integration is added
        appointment.setAppointmentDate(LocalDate.now().plusDays(1));
        appointment.setStartTime(LocalTime.of(10, 0));
        appointment.setEndTime(LocalTime.of(10, 30));

        appointment.setStatus(AppointmentStatus.BOOKED);
        appointment.setReason(request.getReason());
        appointment.setNotes(request.getNotes());

        Appointment saved = appointmentRepository.save(appointment);
        return AppointmentMapper.toResponse(saved);
    }

    @Override
    public AppointmentResponse getAppointmentById(UUID id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        return AppointmentMapper.toResponse(appointment);
    }

    @Override
    public List<AppointmentResponse> getAllAppointments() {
        return appointmentRepository.findAll()
                .stream()
                .map(AppointmentMapper::toResponse)
                .toList();
    }

    @Override
    public AppointmentResponse cancelAppointment(UUID id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("Appointment is already cancelled");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);

        Appointment updated = appointmentRepository.save(appointment);
        return AppointmentMapper.toResponse(updated);
    }

    private void validateCreateRequest(CreateAppointmentRequest request) {
        if (request.getPatientId() == null) {
            throw new BadRequestException("patientId is required");
        }

        if (request.getDoctorId() == null) {
            throw new BadRequestException("doctorId is required");
        }

        if (request.getSlotId() == null) {
            throw new BadRequestException("slotId is required");
        }

        if (request.getReason() == null || request.getReason().trim().isEmpty()) {
            throw new BadRequestException("reason is required");
        }
    }
}