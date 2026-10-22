package com.smartaihms.backend.appointment;

import com.smartaihms.backend.auth.CurrentUser;
import com.smartaihms.backend.doctor.Doctor;
import com.smartaihms.backend.doctor.DoctorRepository;
import com.smartaihms.backend.patient.Patient;
import com.smartaihms.backend.patient.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;

@Service
public class AppointmentService {

    public static final Set<String> STATUSES = Set.of("PENDING", "CONFIRMED", "COMPLETED", "CANCELLED");

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final CurrentUser currentUser;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              DoctorRepository doctorRepository,
                              PatientRepository patientRepository,
                              CurrentUser currentUser) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.currentUser = currentUser;
    }

    // ---------- create (patient books for himself) ----------
    public Appointment createAppointment(Appointment request) {
        currentUser.requireRole("PATIENT");
        Patient patient = currentUser.patient();

        Doctor doctor = doctorRepository.findById(String.valueOf(request.getDoctorId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please choose a valid doctor"));

        LocalDate date;
        try {
            date = LocalDate.parse(request.getAppointmentDate());
            LocalTime.parse(request.getAppointmentTime());
        } catch (DateTimeParseException | NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please give a valid date and time");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment date cannot be in the past");
        }

        boolean slotTaken = appointmentRepository
                .findByDoctorIdAndAppointmentDate(doctor.getId(), request.getAppointmentDate())
                .stream()
                .anyMatch(a -> a.getAppointmentTime().equals(request.getAppointmentTime())
                        && !"CANCELLED".equals(a.getStatus()));
        if (slotTaken) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This doctor is already booked at that time");
        }

        Appointment appointment = new Appointment(
                patient.getId(), doctor.getId(),
                request.getAppointmentDate(), request.getAppointmentTime(),
                request.getReason(), "PENDING");
        return appointmentRepository.save(appointment);
    }

    // ---------- read ----------
    /** Patient: own appointments. Doctor: own appointments. Admin: all. */
    public List<AppointmentView> myAppointments() {
        if (currentUser.isPatient()) {
            return toViews(appointmentRepository.findByPatientId(currentUser.patient().getId()));
        }
        if (currentUser.isDoctor()) {
            return toViews(appointmentRepository.findByDoctorId(currentUser.doctor().getId()));
        }
        return toViews(appointmentRepository.findAll());
    }

    public List<Appointment> getAllAppointments() {
        currentUser.requireRole("ADMIN");
        return appointmentRepository.findAll();
    }

    public List<Appointment> getPatientAppointments(String patientId) {
        currentUser.requirePatientAccess(patientId);
        return appointmentRepository.findByPatientId(patientId);
    }

    public List<Appointment> getDoctorAppointments(String doctorId) {
        if (currentUser.isPatient()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to do this");
        }
        if (currentUser.isDoctor() && !currentUser.doctor().getId().equals(doctorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only see your own appointments");
        }
        return appointmentRepository.findByDoctorId(doctorId);
    }

    public Appointment getAppointmentById(String id) {
        Appointment a = find(id);
        checkCanSee(a);
        return a;
    }

    // ---------- change ----------
    public Appointment updateStatus(String id, String status) {
        String newStatus = status == null ? "" : status.trim().toUpperCase();
        if (!STATUSES.contains(newStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must be one of " + STATUSES);
        }
        Appointment a = find(id);

        if (currentUser.isPatient()) {
            // a patient may only cancel his own appointment
            if (!currentUser.patient().getId().equals(a.getPatientId()) || !"CANCELLED".equals(newStatus)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Patients can only cancel their own appointment");
            }
        } else if (currentUser.isDoctor()) {
            if (!currentUser.doctor().getId().equals(a.getDoctorId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This is not your appointment");
            }
        }
        a.setStatus(newStatus);
        return appointmentRepository.save(a);
    }

    public void deleteAppointment(String id) {
        currentUser.requireRole("ADMIN");
        appointmentRepository.deleteById(id);
    }

    // ---------- helpers ----------
    private Appointment find(String id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
    }

    private void checkCanSee(Appointment a) {
        if (currentUser.isPatient()) {
            currentUser.requirePatientAccess(a.getPatientId());
        } else if (currentUser.isDoctor() && !currentUser.doctor().getId().equals(a.getDoctorId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This is not your appointment");
        }
    }

    private List<AppointmentView> toViews(List<Appointment> list) {
        return list.stream().map(a -> {
            String patientName = patientRepository.findById(a.getPatientId()).map(Patient::getName).orElse("(deleted)");
            Doctor d = doctorRepository.findById(a.getDoctorId()).orElse(null);
            return new AppointmentView(a.getId(), a.getPatientId(), patientName,
                    a.getDoctorId(), d == null ? "(deleted)" : d.getName(),
                    d == null ? "" : d.getSpecialization(),
                    a.getAppointmentDate(), a.getAppointmentTime(), a.getReason(), a.getStatus());
        }).toList();
    }
}
