package com.smartaihms.backend.auth;

import com.smartaihms.backend.doctor.Doctor;
import com.smartaihms.backend.doctor.DoctorRepository;
import com.smartaihms.backend.patient.Patient;
import com.smartaihms.backend.patient.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** Tells services who is logged in (taken from the JWT) and what they may access. */
@Component
public class CurrentUser {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public CurrentUser(UserRepository userRepository,
                       PatientRepository patientRepository,
                       DoctorRepository doctorRepository) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    public String id() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || a.getPrincipal() == null || "anonymousUser".equals(a.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please login");
        }
        return a.getPrincipal().toString();
    }

    public User user() {
        return userRepository.findById(id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    public boolean isAdmin()   { return "ADMIN".equals(user().getRole()); }
    public boolean isDoctor()  { return "DOCTOR".equals(user().getRole()); }
    public boolean isPatient() { return "PATIENT".equals(user().getRole()); }

    /** The patient profile of the logged-in PATIENT. */
    public Patient patient() {
        return patientRepository.findByEmail(user().getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient profile not found"));
    }

    /** The doctor profile of the logged-in DOCTOR. */
    public Doctor doctor() {
        return doctorRepository.findByEmail(user().getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor profile not found"));
    }

    /** Admin and doctors may see any patient; a patient only sees himself. */
    public void requirePatientAccess(String patientId) {
        if (isPatient() && !patient().getId().equals(patientId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only see your own data");
        }
    }

    public void requireRole(String... roles) {
        String role = user().getRole();
        for (String r : roles) {
            if (r.equals(role)) return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to do this");
    }
}
