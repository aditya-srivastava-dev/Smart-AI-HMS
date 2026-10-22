package com.smartaihms.backend.auth;

import com.smartaihms.backend.doctor.DoctorRepository;
import com.smartaihms.backend.patient.PatientRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/** Gives the frontend everything it needs to know about the logged-in user. */
@RestController
@RequestMapping("/api/me")
public class MeController {

    private final CurrentUser currentUser;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public MeController(CurrentUser currentUser, PatientRepository patientRepository,
                        DoctorRepository doctorRepository) {
        this.currentUser = currentUser;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    @GetMapping
    public Map<String, Object> me() {
        User u = currentUser.user();
        Map<String, Object> m = new HashMap<>();
        m.put("id", u.getId());
        m.put("name", u.getName());
        m.put("email", u.getEmail());
        m.put("role", u.getRole());
        patientRepository.findByEmail(u.getEmail()).ifPresent(p -> m.put("patientId", p.getId()));
        doctorRepository.findByEmail(u.getEmail()).ifPresent(d -> m.put("doctorId", d.getId()));
        return m;
    }
}
