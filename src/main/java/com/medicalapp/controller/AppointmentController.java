package com.medicalapp.controller;

import com.medicalapp.dto.ApiResponse;
import com.medicalapp.dto.AppointmentDto;
import com.medicalapp.dto.AppointmentRequest;
import com.medicalapp.dto.AppointmentStatusRequest;
import com.medicalapp.model.AppointmentStatus;
import com.medicalapp.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentDto>> bookAppointment(
            @Valid @RequestBody AppointmentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        AppointmentDto appointment = appointmentService.bookAppointment(request, userDetails.getUsername());
        return new ResponseEntity<>(ApiResponse.ok("Appointment booked successfully", appointment), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AppointmentDto>>> getAppointments(
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long doctorId,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<AppointmentDto> list = appointmentService.getAppointmentsForCurrentUser(
                userDetails.getUsername(), status, date, doctorId);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentDto>> getAppointmentById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        AppointmentDto dto = appointmentService.getAppointmentById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentDto>> updateAppointmentStatus(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentStatusRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        AppointmentDto updated = appointmentService.updateAppointmentStatus(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Appointment updated successfully", updated));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentDto>> patchAppointmentStatus(
            @PathVariable Long id,
            @RequestBody AppointmentStatusRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        AppointmentDto updated = appointmentService.updateAppointmentStatus(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Appointment updated successfully", updated));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<AppointmentDto>> cancelAppointment(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        String reason = (body != null) ? body.get("reason") : null;
        AppointmentDto cancelled = appointmentService.cancelAppointment(id, reason, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Appointment cancelled successfully", cancelled));
    }
}
