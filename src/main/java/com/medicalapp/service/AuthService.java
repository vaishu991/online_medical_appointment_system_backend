package com.medicalapp.service;

import com.medicalapp.dto.AuthResponse;
import com.medicalapp.dto.LoginRequest;
import com.medicalapp.dto.RegisterRequest;
import com.medicalapp.exception.BadRequestException;
import com.medicalapp.exception.ResourceNotFoundException;
import com.medicalapp.model.Doctor;
import com.medicalapp.model.Patient;
import com.medicalapp.model.Role;
import com.medicalapp.model.User;
import com.medicalapp.repository.DoctorRepository;
import com.medicalapp.repository.PatientRepository;
import com.medicalapp.repository.SpecialtyRepository;
import com.medicalapp.repository.UserRepository;
import com.medicalapp.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final SpecialtyRepository specialtyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       PatientRepository patientRepository,
                       DoctorRepository doctorRepository,
                       SpecialtyRepository specialtyRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.specialtyRepository = specialtyRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().trim().toLowerCase(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isActive()) {
            throw new BadRequestException("This account has been deactivated. Please contact an administrator.");
        }

        String token = tokenProvider.generateToken(user.getEmail(), user.getRole().name(), user.getId());

        Long patientId = null;
        Long doctorId = null;

        if (user.getRole() == Role.PATIENT) {
            patientId = patientRepository.findByUserId(user.getId())
                    .map(Patient::getId)
                    .orElse(null);
        } else if (user.getRole() == Role.DOCTOR) {
            doctorId = doctorRepository.findByUserId(user.getId())
                    .map(Doctor::getId)
                    .orElse(null);
        }

        return new AuthResponse(token, user.getId(), user.getEmail(), user.getFullName(),
                user.getPhone(), user.getRole(), patientId, doctorId);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("An account with this email already exists");
        }

        Role assignedRole = request.getRole() != null ? request.getRole() : Role.PATIENT;
        // Restrict self-registration to PATIENT unless specified for demo/setup
        if (assignedRole != Role.PATIENT && assignedRole != Role.DOCTOR) {
            assignedRole = Role.PATIENT;
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone());
        user.setRole(assignedRole);
        user.setActive(true);

        User savedUser = userRepository.save(user);

        Long patientId = null;
        Long doctorId = null;

        if (assignedRole == Role.PATIENT) {
            Patient patient = new Patient();
            patient.setUser(savedUser);
            patient.setDateOfBirth(request.getDateOfBirth());
            patient.setGender(request.getGender());
            patient.setBloodGroup(request.getBloodGroup());
            patient.setAddress(request.getAddress());
            patient.setEmergencyContact(request.getEmergencyContact());
            Patient savedPatient = patientRepository.save(patient);
            patientId = savedPatient.getId();
        }

        String token = tokenProvider.generateToken(savedUser.getEmail(), savedUser.getRole().name(), savedUser.getId());

        return new AuthResponse(token, savedUser.getId(), savedUser.getEmail(), savedUser.getFullName(),
                savedUser.getPhone(), savedUser.getRole(), patientId, doctorId);
    }

    public AuthResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Long patientId = null;
        Long doctorId = null;

        if (user.getRole() == Role.PATIENT) {
            patientId = patientRepository.findByUserId(user.getId())
                    .map(Patient::getId)
                    .orElse(null);
        } else if (user.getRole() == Role.DOCTOR) {
            doctorId = doctorRepository.findByUserId(user.getId())
                    .map(Doctor::getId)
                    .orElse(null);
        }

        String token = tokenProvider.generateToken(user.getEmail(), user.getRole().name(), user.getId());

        return new AuthResponse(token, user.getId(), user.getEmail(), user.getFullName(),
                user.getPhone(), user.getRole(), patientId, doctorId);
    }
}
