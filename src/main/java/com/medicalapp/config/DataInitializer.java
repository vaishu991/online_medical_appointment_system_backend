package com.medicalapp.config;

import com.medicalapp.model.*;
import com.medicalapp.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SpecialtyRepository specialtyRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           SpecialtyRepository specialtyRepository,
                           DoctorRepository doctorRepository,
                           PatientRepository patientRepository,
                           DoctorAvailabilityRepository availabilityRepository,
                           AppointmentRepository appointmentRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.specialtyRepository = specialtyRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.availabilityRepository = availabilityRepository;
        this.appointmentRepository = appointmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // Seed data already present
        }

        // 1. Create Specialties
        Specialty cardio = specialtyRepository.save(new Specialty(
                "Cardiology",
                "Comprehensive heart health, ECG evaluations, hypertension, and cardiovascular wellness.",
                "Heart"
        ));
        Specialty pediatrics = specialtyRepository.save(new Specialty(
                "Pediatrics",
                "Compassionate newborn, child, and adolescent healthcare, immunizations, and developmental checkups.",
                "Baby"
        ));
        Specialty dermatology = specialtyRepository.save(new Specialty(
                "Dermatology",
                "Expert clinical and aesthetic care for skin conditions, acne, eczema, and skin cancer screenings.",
                "Sparkles"
        ));
        Specialty genMed = specialtyRepository.save(new Specialty(
                "General Medicine",
                "Primary healthcare, preventive physicals, acute illness treatment, and lifestyle wellness.",
                "Stethoscope"
        ));
        Specialty ortho = specialtyRepository.save(new Specialty(
                "Orthopedics",
                "Musculoskeletal health, bone fractures, joint pain management, and sports injury recovery.",
                "Activity"
        ));
        Specialty neurology = specialtyRepository.save(new Specialty(
                "Neurology",
                "Diagnostic evaluations and management of migraine, spine, neuropathy, and neurological health.",
                "Brain"
        ));

        // 3. Create Doctors
        Doctor drSarah = createDoctor(
                "dr.sarah@carepulse.com", "doctor123", "Dr. Sarah Jenkins", "+1 555-0101",
                cardio, "MD, FACC (Cardiology)", 12, new BigDecimal("120.00"),
                "Board-certified cardiologist focused on preventative cardiovascular care, lipid management, and high blood pressure.",
                "Suite 302"
        );

        Doctor drJames = createDoctor(
                "dr.james@carepulse.com", "doctor123", "Dr. James Wilson", "+1 555-0102",
                pediatrics, "MD (Pediatrics), FAAP", 9, new BigDecimal("95.00"),
                "Dedicated pediatrician providing warm, attentive care for children from infancy through teenage years.",
                "Suite 104"
        );

        Doctor drEmily = createDoctor(
                "dr.emily@carepulse.com", "doctor123", "Dr. Emily Chen", "+1 555-0103",
                dermatology, "MD, Board Certified Dermatologist", 8, new BigDecimal("110.00"),
                "Experienced dermatologist specializing in medical skin treatments, allergy management, and full-body exams.",
                "Suite 208"
        );

        Doctor drMarcus = createDoctor(
                "dr.marcus@carepulse.com", "doctor123", "Dr. Marcus Patel", "+1 555-0104",
                genMed, "MBBS, MD (Internal Medicine)", 15, new BigDecimal("85.00"),
                "Experienced primary care physician helping families lead healthier lives with preventive strategies.",
                "Suite 101"
        );

        // 4. Create Doctor Availabilities (Mon - Fri)
        List<DayOfWeek> weekDays = Arrays.asList(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
        );

        for (Doctor doc : Arrays.asList(drSarah, drJames, drEmily, drMarcus)) {
            for (DayOfWeek day : weekDays) {
                availabilityRepository.save(new DoctorAvailability(
                        doc, day, LocalTime.of(9, 0), LocalTime.of(17, 0), 30
                ));
            }
        }

        // 5. Create Patients
        Patient patientJohn = createPatient(
                "patient.john@carepulse.com", "patient123", "John Doe", "+1 555-0201",
                LocalDate.of(1988, 4, 12), "Male", "O+", "742 Evergreen Terrace, Springfield", "+1 555-0299 (Spouse)"
        );

        Patient patientAnna = createPatient(
                "patient.anna@carepulse.com", "patient123", "Anna Smith", "+1 555-0202",
                LocalDate.of(1994, 9, 23), "Female", "A+", "123 Elm Street, Metropolis", "+1 555-0298 (Parent)"
        );

        Patient patientRobert = createPatient(
                "patient.robert@carepulse.com", "patient123", "Robert Taylor", "+1 555-0203",
                LocalDate.of(1976, 11, 5), "Male", "B+", "456 Oak Avenue, Gotham", "+1 555-0297 (Brother)"
        );

        // 6. Create Demo Appointments relative to today
        LocalDate today = LocalDate.now();

        // Today's appointment
        Appointment app1 = new Appointment(
                patientJohn, drSarah, today, LocalTime.of(14, 0),
                "Routine cardiovascular health review and blood pressure assessment"
        );
        app1.setStatus(AppointmentStatus.CONFIRMED);
        app1.setNotes("Patient has logged recent BP readings. Bring logs to appointment.");
        appointmentRepository.save(app1);

        // Tomorrow's appointment
        Appointment app2 = new Appointment(
                patientAnna, drEmily, today.plusDays(1), LocalTime.of(10, 30),
                "Consultation for recurring skin rash on forearms"
        );
        app2.setStatus(AppointmentStatus.PENDING);
        appointmentRepository.save(app2);

        // In 2 days
        Appointment app3 = new Appointment(
                patientRobert, drJames, today.plusDays(2), LocalTime.of(11, 0),
                "Pediatric consultation for seasonal allergy flare-ups"
        );
        app3.setStatus(AppointmentStatus.CONFIRMED);
        appointmentRepository.save(app3);

        // In 4 days
        Appointment app4 = new Appointment(
                patientJohn, drMarcus, today.plusDays(4), LocalTime.of(9, 30),
                "Annual comprehensive physical examination and wellness check"
        );
        app4.setStatus(AppointmentStatus.PENDING);
        appointmentRepository.save(app4);

        // Completed appointment in past (5 days ago)
        Appointment app5 = new Appointment(
                patientJohn, drSarah, today.minusDays(5), LocalTime.of(15, 0),
                "Mild palpitations follow-up examination"
        );
        app5.setStatus(AppointmentStatus.COMPLETED);
        app5.setNotes("ECG was normal with sinus rhythm. Advised adequate hydration and moderate exercise.");
        appointmentRepository.save(app5);

        // Completed appointment in past (12 days ago)
        Appointment app6 = new Appointment(
                patientAnna, drMarcus, today.minusDays(12), LocalTime.of(10, 0),
                "Seasonal respiratory symptoms and cough"
        );
        app6.setStatus(AppointmentStatus.COMPLETED);
        app6.setNotes("Upper respiratory tract irritation. Prescribed saline rinses and recovery rest.");
        appointmentRepository.save(app6);
    }

    private Doctor createDoctor(String email, String password, String name, String phone,
                                Specialty specialty, String qualification, Integer exp, BigDecimal fee,
                                String bio, String room) {
        User user = new User(email, passwordEncoder.encode(password), name, phone, Role.DOCTOR);
        User savedUser = userRepository.save(user);

        Doctor doctor = new Doctor(savedUser, specialty, qualification, exp, fee, bio, room);
        return doctorRepository.save(doctor);
    }

    private Patient createPatient(String email, String password, String name, String phone,
                                  LocalDate dob, String gender, String blood, String address, String emergency) {
        User user = new User(email, passwordEncoder.encode(password), name, phone, Role.PATIENT);
        User savedUser = userRepository.save(user);

        Patient patient = new Patient(savedUser, dob, gender, blood, address, emergency);
        return patientRepository.save(patient);
    }
}
