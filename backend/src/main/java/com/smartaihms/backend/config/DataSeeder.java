package com.smartaihms.backend.config;

import com.smartaihms.backend.auth.User;
import com.smartaihms.backend.auth.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates the first ADMIN account when the database has none. */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${app.admin.name}")
    private String adminName;
    @Value("${app.admin.email}")
    private String adminEmail;
    @Value("${app.admin.password}")
    private String adminPassword;

    public DataSeeder(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        boolean adminExists = userRepository.findAll().stream()
                .anyMatch(u -> "ADMIN".equals(u.getRole()));
        if (adminExists) {
            return;
        }
        User admin = new User(adminName, adminEmail.toLowerCase(),
                passwordEncoder.encode(adminPassword), "ADMIN");
        userRepository.save(admin);
        System.out.println("First admin created: " + adminEmail
                + "  (change the password after the first login)");
    }
}
