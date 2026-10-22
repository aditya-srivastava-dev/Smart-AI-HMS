package com.smartaihms.backend.auth;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody User user) {
        User saved = authService.registerUser(user);
        return new AuthResponse(saved.getId(), saved.getName(), saved.getEmail(), saved.getRole());
    }

    @PostMapping("/login")
    public String login(@RequestBody User user) {
        return authService.generateLoginToken(user.getEmail(), user.getPassword());
    }
}
