package com.medicalapp.service;

import com.medicalapp.dto.AdminDashboardDto;
import com.medicalapp.dto.AppointmentDto;
import com.medicalapp.dto.DoctorDashboardDto;
import com.medicalapp.dto.PatientDashboardDto;
import com.medicalapp.exception.ResourceNotFoundException;
import com.medicalapp.model.*;
import com.medicalapp.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final SpecialtyRepository specialtyRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;

    public DashboardService(UserRepository userRepository,
                            DoctorRepository doctorRepository,
                            PatientRepository patientRepository,
                            SpecialtyRepository specialtyRepository,
                            AppointmentRepository appointmentRepository,
                            AppointmentService appointmentService) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.specialtyRepository = specialtyRepository;
        this.appointmentRepository = appointmentRepository;
        this.appointmentService = appointmentService;
    }

    public PatientDashboardDto getPatientDashboard(String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Patient patient = patientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

        List<Appointment> all = appointmentRepository
                .findByPatientIdOrderByAppointmentDateDescAppointmentTimeDesc(patient.getId());

        LocalDate today = LocalDate.now();

        List<AppointmentDto> upcoming = all.stream()
                .filter(a -> (a.getAppointmentDate().isAfter(today) || a.getAppointmentDate().isEqual(today))
                        && a.getStatus() != AppointmentStatus.CANCELLED
                        && a.getStatus() != AppointmentStatus.COMPLETED)
                .map(appointmentService::mapToDto)
                .collect(Collectors.toList());

        List<AppointmentDto> recent = all.stream()
                .limit(5)
                .map(appointmentService::mapToDto)
                .collect(Collectors.toList());

        long completed = all.stream().filter(a -> a.getStatus() == AppointmentStatus.COMPLETED).count();
        long pending = all.stream().filter(a -> a.getStatus() == AppointmentStatus.PENDING).count();

        PatientDashboardDto dto = new PatientDashboardDto();
        dto.setTotalAppointments(all.size());
        dto.setUpcomingAppointments(upcoming.size());
        dto.setCompletedAppointments(completed);
        dto.setPendingAppointments(pending);
        dto.setNextAppointments(upcoming);
        dto.setRecentHistory(recent);

        return dto;
    }

    public DoctorDashboardDto getDoctorDashboard(String currentUserEmail) {
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Doctor doctor = doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));

        List<Appointment> all = appointmentRepository
                .findByDoctorIdOrderByAppointmentDateDescAppointmentTimeDesc(doctor.getId());

        LocalDate today = LocalDate.now();

        List<AppointmentDto> todayList = all.stream()
                .filter(a -> a.getAppointmentDate().isEqual(today))
                .map(appointmentService::mapToDto)
                .collect(Collectors.toList());

        List<AppointmentDto> upcomingList = all.stream()
                .filter(a -> a.getAppointmentDate().isAfter(today)
                        && a.getStatus() != AppointmentStatus.CANCELLED
                        && a.getStatus() != AppointmentStatus.COMPLETED)
                .limit(10)
                .map(appointmentService::mapToDto)
                .collect(Collectors.toList());

        long todayCount = all.stream().filter(a -> a.getAppointmentDate().isEqual(today)).count();
        long pendingCount = all.stream().filter(a -> a.getStatus() == AppointmentStatus.PENDING).count();
        long confirmedCount = all.stream().filter(a -> a.getStatus() == AppointmentStatus.CONFIRMED).count();
        long completedCount = all.stream().filter(a -> a.getStatus() == AppointmentStatus.COMPLETED).count();

        DoctorDashboardDto dto = new DoctorDashboardDto();
        dto.setTodayAppointmentsCount(todayCount);
        dto.setPendingAppointmentsCount(pendingCount);
        dto.setConfirmedAppointmentsCount(confirmedCount);
        dto.setCompletedAppointmentsCount(completedCount);
        dto.setTotalAppointmentsCount(all.size());
        dto.setTodayAppointments(todayList);
        dto.setUpcomingAppointments(upcomingList);

        return dto;
    }

    public AdminDashboardDto getAdminDashboard() {
        long totalPatients = patientRepository.count();
        long totalDoctors = doctorRepository.countByActiveTrue();
        long totalAppointments = appointmentRepository.count();
        long pending = appointmentRepository.countByStatus(AppointmentStatus.PENDING);
        long confirmed = appointmentRepository.countByStatus(AppointmentStatus.CONFIRMED);
        long completed = appointmentRepository.countByStatus(AppointmentStatus.COMPLETED);
        long cancelled = appointmentRepository.countByStatus(AppointmentStatus.CANCELLED);
        long totalSpecialties = specialtyRepository.count();

        List<AppointmentDto> recent = appointmentRepository.findAllByOrderByAppointmentDateDescAppointmentTimeDesc()
                .stream()
                .limit(10)
                .map(appointmentService::mapToDto)
                .collect(Collectors.toList());

        AdminDashboardDto dto = new AdminDashboardDto();
        dto.setTotalPatients(totalPatients);
        dto.setTotalDoctors(totalDoctors);
        dto.setTotalAppointments(totalAppointments);
        dto.setPendingAppointments(pending);
        dto.setConfirmedAppointments(confirmed);
        dto.setCompletedAppointments(completed);
        dto.setCancelledAppointments(cancelled);
        dto.setTotalSpecialties(totalSpecialties);
        dto.setRecentAppointments(recent);

        return dto;
    }
}
