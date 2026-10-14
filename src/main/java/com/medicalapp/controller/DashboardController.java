package com.medicalapp.controller;

import com.medicalapp.dto.AdminDashboardDto;
import com.medicalapp.dto.ApiResponse;
import com.medicalapp.dto.DoctorDashboardDto;
import com.medicalapp.dto.PatientDashboardDto;
import com.medicalapp.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/patient")
    @PreAuthorize("hasAnyRole('PATIENT', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PatientDashboardDto>> getPatientDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {
        PatientDashboardDto dto = dashboardService.getPatientDashboard(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @GetMapping("/doctor")
    @PreAuthorize("hasAnyRole('DOCTOR', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<DoctorDashboardDto>> getDoctorDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {
        DoctorDashboardDto dto = dashboardService.getDoctorDashboard(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AdminDashboardDto>> getAdminDashboard() {
        AdminDashboardDto dto = dashboardService.getAdminDashboard();
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }
}
