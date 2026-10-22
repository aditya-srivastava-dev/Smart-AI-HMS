package com.smartaihms.backend.record;

public record RecordView(
        String id,
        String patientId,
        String patientName,
        String doctorId,
        String doctorName,
        String appointmentId,
        String diagnosis,
        String prescription,
        String notes,
        String createdAt) {
}
