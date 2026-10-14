package com.medicalapp.service;

import com.medicalapp.dto.AvailabilityDto;
import com.medicalapp.dto.TimeSlotDto;
import com.medicalapp.exception.ResourceNotFoundException;
import com.medicalapp.model.Appointment;
import com.medicalapp.model.AppointmentStatus;
import com.medicalapp.model.Doctor;
import com.medicalapp.model.DoctorAvailability;
import com.medicalapp.model.Role;
import com.medicalapp.model.User;
import com.medicalapp.repository.AppointmentRepository;
import com.medicalapp.repository.DoctorAvailabilityRepository;
import com.medicalapp.repository.DoctorRepository;
import com.medicalapp.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AvailabilityService {

    private final DoctorAvailabilityRepository availabilityRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    public AvailabilityService(DoctorAvailabilityRepository availabilityRepository,
                               DoctorRepository doctorRepository,
                               AppointmentRepository appointmentRepository,
                               UserRepository userRepository) {
        this.availabilityRepository = availabilityRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
    }

    public List<AvailabilityDto> getDoctorAvailabilities(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        return availabilityRepository.findByDoctorIdAndActiveTrue(doctor.getId())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<AvailabilityDto> setDoctorAvailabilities(Long doctorId, List<AvailabilityDto> dtos, String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() != Role.SUPER_ADMIN) {
            if (user.getRole() != Role.DOCTOR) {
                throw new AccessDeniedException("Only a super administrator or the assigned doctor can update this schedule");
            }
            Doctor assignedDoctor = doctorRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));
            if (!assignedDoctor.getId().equals(doctorId)) {
                throw new AccessDeniedException("Doctors can only update their own schedules");
            }
        }
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        availabilityRepository.deleteByDoctorId(doctor.getId());

        List<DoctorAvailability> newAvailabilities = new ArrayList<>();
        for (AvailabilityDto dto : dtos) {
            DoctorAvailability da = new DoctorAvailability();
            da.setDoctor(doctor);
            da.setDayOfWeek(dto.getDayOfWeek());
            da.setStartTime(dto.getStartTime());
            da.setEndTime(dto.getEndTime());
            da.setSlotDurationMinutes(dto.getSlotDurationMinutes() != null ? dto.getSlotDurationMinutes() : 30);
            da.setActive(dto.isActive());
            newAvailabilities.add(da);
        }

        List<DoctorAvailability> saved = availabilityRepository.saveAll(newAvailabilities);
        return saved.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public List<TimeSlotDto> getAvailableSlotsForDate(Long doctorId, LocalDate date) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));

        if (!doctor.isActive()) {
            return new ArrayList<>();
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        Optional<DoctorAvailability> optAvail = availabilityRepository
                .findByDoctorIdAndDayOfWeekAndActiveTrue(doctor.getId(), dayOfWeek);

        if (optAvail.isEmpty()) {
            return new ArrayList<>();
        }

        DoctorAvailability availability = optAvail.get();
        int stepMinutes = availability.getSlotDurationMinutes();
        if (stepMinutes <= 0) {
            stepMinutes = 30;
        }

        // Get all booked appointments for that doctor on that date (excluding cancelled)
        List<Appointment> bookedAppointments = appointmentRepository
                .findByDoctorIdAndAppointmentDateAndStatusNot(doctor.getId(), date, AppointmentStatus.CANCELLED);

        Set<LocalTime> bookedTimes = bookedAppointments.stream()
                .map(Appointment::getAppointmentTime)
                .collect(Collectors.toSet());

        List<TimeSlotDto> slots = new ArrayList<>();
        LocalTime currentSlotTime = availability.getStartTime();
        LocalTime endTime = availability.getEndTime();
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        while (currentSlotTime.isBefore(endTime)) {
            boolean isBooked = bookedTimes.contains(currentSlotTime);
            boolean isPast = date.isBefore(today) || (date.isEqual(today) && currentSlotTime.isBefore(now));

            boolean available = !isBooked && !isPast;
            String reason = null;
            if (isBooked) {
                reason = "Already booked";
            } else if (isPast) {
                reason = "Time passed";
            }

            TimeSlotDto slotDto = new TimeSlotDto(
                    currentSlotTime,
                    currentSlotTime.format(TIME_FORMATTER),
                    available,
                    reason
            );
            slots.add(slotDto);

            currentSlotTime = currentSlotTime.plusMinutes(stepMinutes);
        }

        return slots;
    }

    private AvailabilityDto mapToDto(DoctorAvailability da) {
        return new AvailabilityDto(
                da.getId(),
                da.getDoctor().getId(),
                da.getDayOfWeek(),
                da.getStartTime(),
                da.getEndTime(),
                da.getSlotDurationMinutes(),
                da.isActive()
        );
    }
}
