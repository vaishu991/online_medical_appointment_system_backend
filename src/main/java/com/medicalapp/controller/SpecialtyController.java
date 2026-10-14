package com.medicalapp.controller;

import com.medicalapp.dto.ApiResponse;
import com.medicalapp.dto.SpecialtyDto;
import com.medicalapp.service.SpecialtyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/specialties")
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    public SpecialtyController(SpecialtyService specialtyService) {
        this.specialtyService = specialtyService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SpecialtyDto>>> getAllSpecialties() {
        List<SpecialtyDto> list = specialtyService.getAllSpecialties();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SpecialtyDto>> getSpecialtyById(@PathVariable Long id) {
        SpecialtyDto dto = specialtyService.getSpecialtyById(id);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SpecialtyDto>> createSpecialty(@Valid @RequestBody SpecialtyDto dto) {
        SpecialtyDto created = specialtyService.createSpecialty(dto);
        return new ResponseEntity<>(ApiResponse.ok("Specialty created successfully", created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SpecialtyDto>> updateSpecialty(@PathVariable Long id,
                                                                     @Valid @RequestBody SpecialtyDto dto) {
        SpecialtyDto updated = specialtyService.updateSpecialty(id, dto);
        return ResponseEntity.ok(ApiResponse.ok("Specialty updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteSpecialty(@PathVariable Long id) {
        specialtyService.deleteSpecialty(id);
        return ResponseEntity.ok(ApiResponse.ok("Specialty deleted successfully", null));
    }
}
