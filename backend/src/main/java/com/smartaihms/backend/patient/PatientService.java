package com.smartaihms.backend.patient;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public Patient createPatient(Patient patient) {
        return patientRepository.save(patient);
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Patient getPatientById(String id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
    }

    public Patient getPatientByEmail(String email) {
        return patientRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
    }

    /** Only name, age, gender and phone can be changed; the email links the profile to the login. */
    public Patient updatePatient(String id, Patient changes) {
        Patient existing = getPatientById(id);
        if (changes.getAge() < 0 || changes.getAge() > 120) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Age must be between 0 and 120");
        }
        if (changes.getName() != null && !changes.getName().isBlank()) {
            existing.setName(changes.getName().trim());
        }
        existing.setAge(changes.getAge());
        existing.setGender(changes.getGender());
        existing.setPhone(changes.getPhone());
        return patientRepository.save(existing);
    }

    public void deletePatient(String id) {
        patientRepository.deleteById(id);
    }
}
