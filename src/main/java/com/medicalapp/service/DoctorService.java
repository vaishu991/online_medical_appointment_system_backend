package com.medicalapp.service;

import com.medicalapp.dto.DoctorDto;
import com.medicalapp.dto.DoctorRequest;
import com.medicalapp.exception.BadRequestException;
import com.medicalapp.exception.ResourceNotFoundException;
import com.medicalapp.model.Doctor;
import com.medicalapp.model.Role;
import com.medicalapp.model.Specialty;
import com.medicalapp.model.User;
import com.medicalapp.repository.DoctorRepository;
import com.medicalapp.repository.SpecialtyRepository;
import com.medicalapp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final SpecialtyRepository specialtyRepository;
    private final PasswordEncoder passwordEncoder;

    public DoctorService(DoctorRepository doctorRepository,
                         UserRepository userRepository,
                         SpecialtyRepository specialtyRepository,
                         PasswordEncoder passwordEncoder) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.specialtyRepository = specialtyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<DoctorDto> getAllDoctors(String search, Long specialtyId, Boolean includeInactive) {
        List<Doctor> doctors;

        if (Boolean.TRUE.equals(includeInactive)) {
            doctors = doctorRepository.findAll();
        } else {
            String trimmedSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
            doctors = doctorRepository.searchDoctors(trimmedSearch, specialtyId);
        }

        return doctors.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public DoctorDto getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
        return mapToDto(doctor);
    }

    public DoctorDto getDoctorByUserId(Long userId) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for user: " + userId));
        return mapToDto(doctor);
    }

    @Transactional
    public DoctorDto createDoctor(DoctorRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("An account with this email already exists");
        }

        Specialty specialty = specialtyRepository.findById(request.getSpecialtyId())
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found with id: " + request.getSpecialtyId()));

        String rawPassword = (request.getPassword() != null && !request.getPassword().trim().isEmpty())
                ? request.getPassword().trim()
                : "doctor123";

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone());
        user.setRole(Role.DOCTOR);
        user.setActive(true);

        User savedUser = userRepository.save(user);

        Doctor doctor = new Doctor();
        doctor.setUser(savedUser);
        doctor.setSpecialty(specialty);
        doctor.setQualification(request.getQualification());
        doctor.setExperienceYears(request.getExperienceYears());
        doctor.setConsultationFee(request.getConsultationFee());
        doctor.setBio(request.getBio());
        doctor.setRoomNumber(request.getRoomNumber());
        doctor.setActive(request.getActive() != null ? request.getActive() : true);

        Doctor savedDoctor = doctorRepository.save(doctor);
        return mapToDto(savedDoctor);
    }

    @Transactional
    public DoctorDto updateDoctor(Long id, DoctorRequest request) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));

        User user = doctor.getUser();
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        userRepository.save(user);

        if (request.getSpecialtyId() != null) {
            Specialty specialty = specialtyRepository.findById(request.getSpecialtyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Specialty not found with id: " + request.getSpecialtyId()));
            doctor.setSpecialty(specialty);
        }

        if (request.getQualification() != null) {
            doctor.setQualification(request.getQualification());
        }
        if (request.getExperienceYears() != null) {
            doctor.setExperienceYears(request.getExperienceYears());
        }
        if (request.getConsultationFee() != null) {
            doctor.setConsultationFee(request.getConsultationFee());
        }
        if (request.getBio() != null) {
            doctor.setBio(request.getBio());
        }
        if (request.getRoomNumber() != null) {
            doctor.setRoomNumber(request.getRoomNumber());
        }
        if (request.getActive() != null) {
            doctor.setActive(request.getActive());
        }

        Doctor saved = doctorRepository.save(doctor);
        return mapToDto(saved);
    }

    @Transactional
    public DoctorDto toggleDoctorActive(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
        doctor.setActive(!doctor.isActive());
        Doctor saved = doctorRepository.save(doctor);
        return mapToDto(saved);
    }

    public DoctorDto mapToDto(Doctor doctor) {
        DoctorDto dto = new DoctorDto();
        dto.setId(doctor.getId());
        dto.setUserId(doctor.getUser().getId());
        dto.setFullName(doctor.getUser().getFullName());
        dto.setEmail(doctor.getUser().getEmail());
        dto.setPhone(doctor.getUser().getPhone());
        dto.setSpecialtyId(doctor.getSpecialty().getId());
        dto.setSpecialtyName(doctor.getSpecialty().getName());
        dto.setQualification(doctor.getQualification());
        dto.setExperienceYears(doctor.getExperienceYears());
        dto.setConsultationFee(doctor.getConsultationFee());
        dto.setBio(doctor.getBio());
        dto.setRoomNumber(doctor.getRoomNumber());
        dto.setActive(doctor.isActive());
        return dto;
    }
}
