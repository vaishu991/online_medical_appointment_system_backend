package com.medicalapp.controller;

import com.medicalapp.dto.ApiResponse;
import com.medicalapp.dto.AvailabilityDto;
import com.medicalapp.dto.DoctorDto;
import com.medicalapp.dto.DoctorRequest;
import com.medicalapp.service.AvailabilityService;
import com.medicalapp.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;
    private final AvailabilityService availabilityService;

    public DoctorController(DoctorService doctorService, AvailabilityService availabilityService) {
        this.doctorService = doctorService;
        this.availabilityService = availabilityService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DoctorDto>>> getAllDoctors(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long specialtyId,
            @RequestParam(required = false, defaultValue = "false") Boolean includeInactive,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (Boolean.TRUE.equals(includeInactive) && (userDetails == null || userDetails.getAuthorities().stream()
                .noneMatch(authority -> authority.getAuthority().equals("ROLE_SUPER_ADMIN")))) {
            throw new AccessDeniedException("Only a super administrator may view inactive doctor records");
        }
        List<DoctorDto> doctors = doctorService.getAllDoctors(search, specialtyId, includeInactive);
        return ResponseEntity.ok(ApiResponse.ok(doctors));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DoctorDto>> getDoctorById(@PathVariable Long id) {
        DoctorDto doctor = doctorService.getDoctorById(id);
        return ResponseEntity.ok(ApiResponse.ok(doctor));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<DoctorDto>> createDoctor(@Valid @RequestBody DoctorRequest request) {
        DoctorDto created = doctorService.createDoctor(request);
        return new ResponseEntity<>(ApiResponse.ok("Doctor registered successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<DoctorDto>> updateDoctor(@PathVariable Long id,
                                                               @RequestBody DoctorRequest request) {
        DoctorDto updated = doctorService.updateDoctor(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Doctor updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<DoctorDto>> toggleDoctorStatus(@PathVariable Long id) {
        DoctorDto updated = doctorService.toggleDoctorActive(id);
        return ResponseEntity.ok(ApiResponse.ok("Doctor status updated", updated));
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<?>> getDoctorAvailability(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date != null) {
            return ResponseEntity.ok(ApiResponse.ok(availabilityService.getAvailableSlotsForDate(id, date)));
        }
        return ResponseEntity.ok(ApiResponse.ok(availabilityService.getDoctorAvailabilities(id)));
    }

    @PutMapping("/{id}/availability")
    @PreAuthorize("hasAnyRole('DOCTOR', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<AvailabilityDto>>> updateDoctorAvailability(
            @PathVariable Long id,
            @RequestBody List<AvailabilityDto> availabilities,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<AvailabilityDto> updated = availabilityService.setDoctorAvailabilities(
                id, availabilities, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Doctor schedule updated successfully", updated));
    }
}