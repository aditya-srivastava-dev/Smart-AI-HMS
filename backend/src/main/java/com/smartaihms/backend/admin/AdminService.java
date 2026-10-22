package com.smartaihms.backend.admin;

import com.smartaihms.backend.appointment.Appointment;
import com.smartaihms.backend.appointment.AppointmentRepository;
import com.smartaihms.backend.auth.User;
import com.smartaihms.backend.auth.UserRepository;
import com.smartaihms.backend.doctor.Doctor;
import com.smartaihms.backend.doctor.DoctorRepository;
import com.smartaihms.backend.patient.Patient;
import com.smartaihms.backend.patient.PatientRepository;
import com.smartaihms.backend.prediction.Prediction;
import com.smartaihms.backend.prediction.PredictionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final PredictionRepository predictionRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminService(UserRepository userRepository, PatientRepository patientRepository,
                        DoctorRepository doctorRepository, AppointmentRepository appointmentRepository,
                        PredictionRepository predictionRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.predictionRepository = predictionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Numbers for the admin dashboard. */
    public Map<String, Object> stats() {
        List<Appointment> appointments = appointmentRepository.findAll();
        Map<String, Long> byStatus = appointments.stream()
                .collect(Collectors.groupingBy(Appointment::getStatus, TreeMap::new, Collectors.counting()));

        List<Prediction> predictions = predictionRepository.findAll();
        Map<String, Long> diseaseCount = predictions.stream()
                .filter(p -> p.getResults() != null && !p.getResults().isEmpty())
                .collect(Collectors.groupingBy(p -> p.getResults().get(0).disease(), Collectors.counting()));
        List<Map<String, Object>> topDiseases = diseaseCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> Map.<String, Object>of("disease", e.getKey(), "count", e.getValue()))
                .toList();

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("users", userRepository.count());
        m.put("patients", patientRepository.count());
        m.put("doctors", doctorRepository.count());
        m.put("appointments", appointments.size());
        m.put("appointmentsByStatus", byStatus);
        m.put("predictions", predictions.size());
        m.put("topPredictedDiseases", topDiseases);
        return m;
    }

    /** Creates the doctor's login account and the doctor profile together. */
    public Map<String, String> createDoctor(DoctorCreateRequest r) {
        if (isBlank(r.name()) || isBlank(r.email()) || isBlank(r.password())
                || isBlank(r.specialization()) || isBlank(r.licenseNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Name, email, password, specialization and license number are required");
        }
        if (r.password().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters");
        }
        String email = r.email().trim().toLowerCase();
        if (userRepository.findByEmail(email).isPresent() || doctorRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This email is already used");
        }
        if (doctorRepository.existsByLicenseNumber(r.licenseNumber().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "License number already exists");
        }

        userRepository.save(new User(r.name().trim(), email, passwordEncoder.encode(r.password()), "DOCTOR"));
        Doctor d = doctorRepository.save(new Doctor(r.name().trim(), email, r.specialization().trim(),
                r.phone(), r.licenseNumber().trim()));
        return Map.of("message", "Doctor account created", "doctorId", d.getId());
    }

    public List<UserView> users() {
        return userRepository.findAll().stream()
                .map(u -> new UserView(u.getId(), u.getName(), u.getEmail(), u.getRole()))
                .toList();
    }

    /** Deletes the patient profile and the login account. */
    public Map<String, String> deletePatient(String id) {
        Patient p = patientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
        userRepository.findByEmail(p.getEmail()).ifPresent(userRepository::delete);
        patientRepository.deleteById(id);
        return Map.of("message", "Patient deleted");
    }

    public Map<String, String> resetPassword(PasswordResetRequest r) {
        if (isBlank(r.email()) || isBlank(r.newPassword()) || r.newPassword().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and a password of 6+ characters are required");
        }
        User user = userRepository.findByEmail(r.email().trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.setPassword(passwordEncoder.encode(r.newPassword()));
        userRepository.save(user);
        return Map.of("message", "Password reset successfully");
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
