package com.smartaihms.backend.record;

public record RecordRequest(
        String patientId,
        String appointmentId,
        String diagnosis,
        String prescription,
        String notes) {
}
