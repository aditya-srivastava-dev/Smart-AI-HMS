package com.smartaihms.backend.appointment;

/** An appointment together with the patient and doctor names, ready for the screens. */
public record AppointmentView(
        String id,
        String patientId,
        String patientName,
        String doctorId,
        String doctorName,
        String specialization,
        String appointmentDate,
        String appointmentTime,
        String reason,
        String status) {
}
