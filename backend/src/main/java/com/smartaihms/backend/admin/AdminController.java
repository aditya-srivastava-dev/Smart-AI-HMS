package com.smartaihms.backend.admin;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Everything under /api/admin is for the ADMIN role only (see SecurityConfig). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return adminService.stats();
    }

    @PostMapping("/doctors")
    public Map<String, String> createDoctor(@RequestBody DoctorCreateRequest request) {
        return adminService.createDoctor(request);
    }

    @GetMapping("/users")
    public List<UserView> users() {
        return adminService.users();
    }

    @DeleteMapping("/patients/{id}")
    public Map<String, String> deletePatient(@PathVariable String id) {
        return adminService.deletePatient(id);
    }

    @PostMapping("/users/reset-password")
    public Map<String, String> resetPassword(@RequestBody PasswordResetRequest request) {
        return adminService.resetPassword(request);
    }
}
