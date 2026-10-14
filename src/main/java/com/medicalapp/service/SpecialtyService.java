package com.medicalapp.service;

import com.medicalapp.dto.SpecialtyDto;
import com.medicalapp.exception.BadRequestException;
import com.medicalapp.exception.ResourceNotFoundException;
import com.medicalapp.model.Specialty;
import com.medicalapp.repository.DoctorRepository;
import com.medicalapp.repository.SpecialtyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SpecialtyService {

    private final SpecialtyRepository specialtyRepository;
    private final DoctorRepository doctorRepository;

    public SpecialtyService(SpecialtyRepository specialtyRepository, DoctorRepository doctorRepository) {
        this.specialtyRepository = specialtyRepository;
        this.doctorRepository = doctorRepository;
    }

    public List<SpecialtyDto> getAllSpecialties() {
        return specialtyRepository.findAll().stream().map(s -> {
            long count = doctorRepository.findBySpecialtyIdAndActiveTrue(s.getId()).size();
            return new SpecialtyDto(s.getId(), s.getName(), s.getDescription(), s.getIcon(), count);
        }).collect(Collectors.toList());
    }

    public SpecialtyDto getSpecialtyById(Long id) {
        Specialty s = specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found with id: " + id));
        long count = doctorRepository.findBySpecialtyIdAndActiveTrue(s.getId()).size();
        return new SpecialtyDto(s.getId(), s.getName(), s.getDescription(), s.getIcon(), count);
    }

    @Transactional
    public SpecialtyDto createSpecialty(SpecialtyDto dto) {
        if (specialtyRepository.existsByNameIgnoreCase(dto.getName().trim())) {
            throw new BadRequestException("Specialty with this name already exists");
        }

        Specialty specialty = new Specialty();
        specialty.setName(dto.getName().trim());
        specialty.setDescription(dto.getDescription());
        specialty.setIcon(dto.getIcon() != null ? dto.getIcon() : "Activity");

        Specialty saved = specialtyRepository.save(specialty);
        return new SpecialtyDto(saved.getId(), saved.getName(), saved.getDescription(), saved.getIcon(), 0L);
    }

    @Transactional
    public SpecialtyDto updateSpecialty(Long id, SpecialtyDto dto) {
        Specialty specialty = specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found with id: " + id));

        if (!specialty.getName().equalsIgnoreCase(dto.getName().trim()) &&
                specialtyRepository.existsByNameIgnoreCase(dto.getName().trim())) {
            throw new BadRequestException("Another specialty with this name already exists");
        }

        specialty.setName(dto.getName().trim());
        specialty.setDescription(dto.getDescription());
        if (dto.getIcon() != null) {
            specialty.setIcon(dto.getIcon());
        }

        Specialty updated = specialtyRepository.save(specialty);
        long count = doctorRepository.findBySpecialtyIdAndActiveTrue(updated.getId()).size();
        return new SpecialtyDto(updated.getId(), updated.getName(), updated.getDescription(), updated.getIcon(), count);
    }

    @Transactional
    public void deleteSpecialty(Long id) {
        Specialty specialty = specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found with id: " + id));

        long doctorsCount = doctorRepository.findBySpecialtyIdAndActiveTrue(id).size();
        if (doctorsCount > 0) {
            throw new BadRequestException("Cannot delete specialty with " + doctorsCount + " assigned active doctors");
        }

        specialtyRepository.delete(specialty);
    }
}
