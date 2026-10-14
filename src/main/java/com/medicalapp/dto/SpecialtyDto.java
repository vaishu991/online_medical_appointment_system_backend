package com.medicalapp.dto;

import jakarta.validation.constraints.NotBlank;

public class SpecialtyDto {
    private Long id;

    @NotBlank(message = "Specialty name is required")
    private String name;

    private String description;
    private String icon;
    private Long doctorCount;

    public SpecialtyDto() {
    }

    public SpecialtyDto(Long id, String name, String description, String icon, Long doctorCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.doctorCount = doctorCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Long getDoctorCount() {
        return doctorCount;
    }

    public void setDoctorCount(Long doctorCount) {
        this.doctorCount = doctorCount;
    }
}
