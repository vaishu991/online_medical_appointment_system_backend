package com.medicalapp.service;

import com.medicalapp.dto.AppointmentDto;
import com.medicalapp.dto.AppointmentRequest;
import com.medicalapp.dto.AppointmentStatusRequest;
import com.medicalapp.exception.BadRequestException;
import com.medicalapp.exception.ResourceNotFoundException;
import com.medicalapp.model.*;
import com.medicalapp.repository.AppointmentRepository;
import com.medicalapp.repository.DoctorRepository;
import com.medicalapp.repository.PatientRepository;
import com.medicalapp.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              DoctorRepository doctorRepository,
                              PatientRepository patientRepository,
                              UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AppointmentDto bookAppointment(AppointmentRequest request, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Patient patient;
        if (user.getRole() == Role.PATIENT) {
            patient = patientRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new BadRequestException("Patient profile not found for user"));
        } else if (user.getRole() == Role.SUPER_ADMIN) {
            if (request.getPatientId() == null) {
                throw new BadRequestException("Super administrator must specify patientId to book an appointment");
            }
            patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + request.getPatientId()));
        } else {
            throw new AccessDeniedException("Doctors cannot book appointments directly");
        }

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + request.getDoctorId()));

        if (!doctor.isActive()) {
            throw new BadRequestException("Doctor is currently inactive and cannot accept appointments");
        }

        // Validate appointment is in the future
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (request.getAppointmentDate().isBefore(today)) {
            throw new BadRequestException("Appointment date must be today or in the future");
        }

        if (request.getAppointmentDate().isEqual(today) && request.getAppointmentTime().isBefore(now)) {
            throw new BadRequestException("Appointment time must be in the future");
        }

        // Check if doctor is already booked at that time (Rule: A patient cannot book a time slot that is already occupied)
        boolean isOccupied = appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
                doctor.getId(),
                request.getAppointmentDate(),
                request.getAppointmentTime(),
                AppointmentStatus.CANCELLED
        );

        if (isOccupied) {
            throw new BadRequestException("The selected doctor is already booked at this date and time slot");
        }

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setAppointmentTime(request.getAppointmentTime());
        appointment.setReason(request.getReason().trim());
        appointment.setStatus(AppointmentStatus.PENDING);

        Appointment saved = appointmentRepository.save(appointment);
        return mapToDto(saved);
    }

    public List<AppointmentDto> getAppointmentsForCurrentUser(String currentUserEmail,
                                                              AppointmentStatus status,
                                                              LocalDate date,
                                                              Long doctorId) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Appointment> list;

        if (user.getRole() == Role.PATIENT) {
            Patient patient = patientRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));
            list = appointmentRepository.findByPatientIdOrderByAppointmentDateDescAppointmentTimeDesc(patient.getId());
            if (status != null) {
                list = list.stream().filter(a -> a.getStatus() == status).collect(Collectors.toList());
            }
            if (date != null) {
                list = list.stream().filter(a -> a.getAppointmentDate().isEqual(date)).collect(Collectors.toList());
            }
        } else if (user.getRole() == Role.DOCTOR) {
            Doctor doctor = doctorRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));
            list = appointmentRepository.findByDoctorIdOrderByAppointmentDateDescAppointmentTimeDesc(doctor.getId());
            if (status != null) {
                list = list.stream().filter(a -> a.getStatus() == status).collect(Collectors.toList());
            }
            if (date != null) {
                list = list.stream().filter(a -> a.getAppointmentDate().isEqual(date)).collect(Collectors.toList());
            }
        } else if (user.getRole() == Role.SUPER_ADMIN) {
            list = appointmentRepository.filterAppointments(status, date, doctorId);
        } else {
            throw new AccessDeniedException("Unauthorized to access appointments");
        }

        return list.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public AppointmentDto getAppointmentById(Long id, String currentUserEmail) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        validateAccess(appointment, user);

        return mapToDto(appointment);
    }

    @Transactional
    public AppointmentDto updateAppointmentStatus(Long id, AppointmentStatusRequest request, String currentUserEmail) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        validateAccess(appointment, user);

        // Check valid transitions
        AppointmentStatus current = appointment.getStatus();
        AppointmentStatus target = request.getStatus();

        if (current == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("Cannot update a cancelled appointment");
        }

        if (current == AppointmentStatus.COMPLETED && target != AppointmentStatus.COMPLETED) {
            throw new BadRequestException("Cannot change status of an already completed appointment");
        }

        if (user.getRole() == Role.PATIENT) {
            if (target != AppointmentStatus.CANCELLED) {
                throw new AccessDeniedException("Patients can only cancel their appointments");
            }
        }

        appointment.setStatus(target);

        if (request.getNotes() != null) {
            appointment.setNotes(request.getNotes().trim());
        }

        if (target == AppointmentStatus.CANCELLED) {
            String reason = request.getCancellationReason();
            if (reason == null || reason.trim().isEmpty()) {
                reason = "Cancelled by " + user.getRole().name().toLowerCase();
            }
            appointment.setCancellationReason(reason);
        }

        Appointment updated = appointmentRepository.save(appointment);
        return mapToDto(updated);
    }

    @Transactional
    public AppointmentDto cancelAppointment(Long id, String cancellationReason, String currentUserEmail) {
        AppointmentStatusRequest req = new AppointmentStatusRequest();
        req.setStatus(AppointmentStatus.CANCELLED);
        req.setCancellationReason(cancellationReason != null ? cancellationReason : "Cancelled by user");
        return updateAppointmentStatus(id, req, currentUserEmail);
    }

    private void validateAccess(Appointment appointment, User user) {
        if (user.getRole() == Role.SUPER_ADMIN) {
            return;
        }

        if (user.getRole() == Role.PATIENT) {
            if (!appointment.getPatient().getUser().getId().equals(user.getId())) {
                throw new AccessDeniedException("You do not have permission to access this appointment");
            }
            return;
        }

        if (user.getRole() == Role.DOCTOR) {
            if (!appointment.getDoctor().getUser().getId().equals(user.getId())) {
                throw new AccessDeniedException("You do not have permission to access this appointment");
            }
            return;
        }

        throw new AccessDeniedException("Unauthorized to access this appointment");
    }

    public AppointmentDto mapToDto(Appointment a) {
        AppointmentDto dto = new AppointmentDto();
        dto.setId(a.getId());

        // Patient info
        dto.setPatientId(a.getPatient().getId());
        dto.setPatientUserId(a.getPatient().getUser().getId());
        dto.setPatientName(a.getPatient().getUser().getFullName());
        dto.setPatientEmail(a.getPatient().getUser().getEmail());
        dto.setPatientPhone(a.getPatient().getUser().getPhone());
        dto.setPatientGender(a.getPatient().getGender());
        dto.setPatientBloodGroup(a.getPatient().getBloodGroup());

        // Doctor info
        dto.setDoctorId(a.getDoctor().getId());
        dto.setDoctorUserId(a.getDoctor().getUser().getId());
        dto.setDoctorName(a.getDoctor().getUser().getFullName());
        dto.setDoctorEmail(a.getDoctor().getUser().getEmail());
        dto.setDoctorPhone(a.getDoctor().getUser().getPhone());
        dto.setSpecialtyName(a.getDoctor().getSpecialty().getName());
        dto.setQualification(a.getDoctor().getQualification());
        dto.setConsultationFee(a.getDoctor().getConsultationFee());
        dto.setRoomNumber(a.getDoctor().getRoomNumber());

        // Appointment details
        dto.setAppointmentDate(a.getAppointmentDate());
        dto.setAppointmentTime(a.getAppointmentTime());
        dto.setReason(a.getReason());
        dto.setStatus(a.getStatus());
        dto.setCancellationReason(a.getCancellationReason());
        dto.setNotes(a.getNotes());
        dto.setCreatedAt(a.getCreatedAt());
        dto.setUpdatedAt(a.getUpdatedAt());

        return dto;
    }
}
