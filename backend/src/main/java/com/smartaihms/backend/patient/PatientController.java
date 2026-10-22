package com.smartaihms.backend.patient;

import com.smartaihms.backend.auth.CurrentUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;
    private final CurrentUser currentUser;

    public PatientController(PatientService patientService, CurrentUser currentUser) {
        this.patientService = patientService;
        this.currentUser = currentUser;
    }

    // Patients are created by registration; admin may also add one directly
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Patient createPatient(@RequestBody Patient patient) {
        return patientService.createPatient(patient);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public List<Patient> getAllPatients() {
        return patientService.getAllPatients();
    }

    // The logged-in patient's own profile
    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public Patient myProfile() {
        return currentUser.patient();
    }

    @GetMapping("/{id}")
    public Patient getPatientById(@PathVariable String id) {
        currentUser.requirePatientAccess(id);
        return patientService.getPatientById(id);
    }

    @GetMapping("/email/{email}")
    public Patient getPatientByEmail(@PathVariable String email) {
        Patient p = patientService.getPatientByEmail(email);
        currentUser.requirePatientAccess(p.getId());
        return p;
    }

    @PutMapping("/{id}")
    public Patient updatePatient(@PathVariable String id, @RequestBody Patient patient) {
        currentUser.requirePatientAccess(id);
        return patientService.updatePatient(id, patient);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deletePatient(@PathVariable String id) {
        patientService.deletePatient(id);
        return "Patient deleted successfully";
    }
}
