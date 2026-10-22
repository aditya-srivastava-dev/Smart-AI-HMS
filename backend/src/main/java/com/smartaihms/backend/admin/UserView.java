package com.smartaihms.backend.admin;

/** A user without the password hash. */
public record UserView(String id, String name, String email, String role) {
}
