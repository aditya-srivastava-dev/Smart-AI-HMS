package com.smartaihms.backend.record;

import com.smartaihms.backend.appointment.Appointment;
import com.smartaihms.backend.appointment.AppointmentRepository;
import com.smartaihms.backend.auth.CurrentUser;
import com.smartaihms.backend.doctor.Doctor;
import com.smartaihms.backend.doctor.DoctorRepository;
import com.smartaihms.backend.patient.Patient;
import com.smartaihms.backend.patient.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class RecordService {

    private final MedicalRecordRepository recordRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final CurrentUser currentUser;

    public RecordService(MedicalRecordRepository recordRepository,
                         AppointmentRepository appointmentRepository,
                         PatientRepository patientRepository,
                         DoctorRepository doctorRepository,
                         CurrentUser currentUser) {
        this.recordRepository = recordRepository;
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.currentUser = currentUser;
    }

    /** A doctor writes a record. If it belongs to an appointment, that appointment becomes COMPLETED. */
    public RecordView create(RecordRequest req) {
        currentUser.requireRole("DOCTOR");
        Doctor doctor = currentUser.doctor();

        if (req.patientId() == null || patientRepository.findById(req.patientId()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please choose a valid patient");
        }
        if (req.diagnosis() == null || req.diagnosis().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Diagnosis is required");
        }

        String appointmentId = null;
        if (req.appointmentId() != null && !req.appointmentId().isBlank()) {
            Appointment a = appointmentRepository.findById(req.appointmentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment not found"));
            if (!a.getDoctorId().equals(doctor.getId()) || !a.getPatientId().equals(req.patientId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This appointment does not belong to you and this patient");
            }
            a.setStatus("COMPLETED");
            appointmentRepository.save(a);
            appointmentId = a.getId();
        }

        MedicalRecord r = new MedicalRecord();
        r.setPatientId(req.patientId());
        r.setDoctorId(doctor.getId());
        r.setAppointmentId(appointmentId);
        r.setDiagnosis(req.diagnosis().trim());
        r.setPrescription(req.prescription());
        r.setNotes(req.notes());
        r.setCreatedAt(Instant.now().toString());
        return toView(recordRepository.save(r));
    }

    /** Patient: own records. Doctor: records he wrote. Admin: all. */
    public List<RecordView> mine() {
        if (currentUser.isPatient()) {
            return recordRepository.findByPatientId(currentUser.patient().getId()).stream().map(this::toView).toList();
        }
        if (currentUser.isDoctor()) {
            return recordRepository.findByDoctorId(currentUser.doctor().getId()).stream().map(this::toView).toList();
        }
        return recordRepository.findAll().stream().map(this::toView).toList();
    }

    public List<RecordView> forPatient(String patientId) {
        currentUser.requirePatientAccess(patientId);
        return recordRepository.findByPatientId(patientId).stream().map(this::toView).toList();
    }

    private RecordView toView(MedicalRecord r) {
        String patientName = patientRepository.findById(r.getPatientId()).map(Patient::getName).orElse("(deleted)");
        String doctorName = doctorRepository.findById(r.getDoctorId()).map(Doctor::getName).orElse("(deleted)");
        return new RecordView(r.getId(), r.getPatientId(), patientName, r.getDoctorId(), doctorName,
                r.getAppointmentId(), r.getDiagnosis(), r.getPrescription(), r.getNotes(), r.getCreatedAt());
    }
}
