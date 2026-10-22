package com.smartaihms.backend.appointment;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AppointmentRepository extends MongoRepository<Appointment, String> {

    List<Appointment> findByPatientId(String patientId);

    List<Appointment> findByDoctorId(String doctorId);

    List<Appointment> findByStatus(String status);

    List<Appointment> findByDoctorIdAndAppointmentDate(String doctorId, String appointmentDate);
}
