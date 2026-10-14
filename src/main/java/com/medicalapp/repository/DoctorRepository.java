package com.medicalapp.repository;

import com.medicalapp.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUserId(Long userId);
    Optional<Doctor> findByUserEmail(String email);
    List<Doctor> findByActiveTrue();
    List<Doctor> findBySpecialtyIdAndActiveTrue(Long specialtyId);

    @Query("SELECT d FROM Doctor d WHERE d.active = true " +
           "AND (:specialtyId IS NULL OR d.specialty.id = :specialtyId) " +
           "AND (:search IS NULL OR LOWER(d.user.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "     OR LOWER(d.specialty.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "     OR LOWER(d.qualification) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Doctor> searchDoctors(@Param("search") String search, @Param("specialtyId") Long specialtyId);

    long countByActiveTrue();
}
