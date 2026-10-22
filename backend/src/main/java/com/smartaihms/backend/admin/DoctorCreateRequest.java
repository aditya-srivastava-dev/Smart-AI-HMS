package com.smartaihms.backend.admin;

public record DoctorCreateRequest(
        String name,
        String email,
        String password,
        String specialization,
        String phone,
        String licenseNumber) {
}
