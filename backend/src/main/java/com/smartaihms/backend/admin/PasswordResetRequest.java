package com.smartaihms.backend.admin;

public record PasswordResetRequest(String email, String newPassword) {
}
