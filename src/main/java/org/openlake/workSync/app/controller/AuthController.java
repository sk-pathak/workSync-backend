package org.openlake.workSync.app.controller;

import lombok.RequiredArgsConstructor;
import org.openlake.workSync.app.dto.AuthResponseDTO;
import org.openlake.workSync.app.dto.UserRequestDTO;
import org.openlake.workSync.app.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody UserRequestDTO request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestParam String username, @RequestParam String password) {
        return ResponseEntity.ok(authService.login(username, password));
    }

    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminOnly() {
        return ResponseEntity.ok("You are an admin!");
    }

    @GetMapping("/user-only")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> userOnly() {
        return ResponseEntity.ok("You are a user!");
    }
}
