package org.openlake.projectmanagerbackend.resource;

import lombok.RequiredArgsConstructor;
import org.openlake.projectmanagerbackend.domain.AuthResponse;
import org.openlake.projectmanagerbackend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserResource {

    private final UserService userService;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthResponse> getAllUsers() {
        AuthResponse authResponse = userService.getAllUsers();
        return ResponseEntity.status(authResponse.getStatusCode()).body(authResponse);
    }

    @GetMapping("/all/{username}")
    public ResponseEntity<AuthResponse> getUserById(@PathVariable("username") String username) {
        AuthResponse authResponse = userService.getUserByUsername(username);
        return ResponseEntity.status(authResponse.getStatusCode()).body(authResponse);
    }

    @DeleteMapping("/all/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthResponse> deleteUser(@PathVariable("username") String username) {
        AuthResponse authResponse = userService.deleteUser(username);
        return ResponseEntity.status(authResponse.getStatusCode()).body(authResponse);
    }
}
