package org.openlake.projectmanagerbackend.resource;

import lombok.RequiredArgsConstructor;
import org.openlake.projectmanagerbackend.domain.AuthResponse;
import org.openlake.projectmanagerbackend.domain.entity.UserEntity;
import org.openlake.projectmanagerbackend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthResource {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody UserEntity userEntity) {
        AuthResponse authResponse = userService.createUser(userEntity);
        return ResponseEntity.status(authResponse.getStatusCode()).body(authResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        AuthResponse authResponse = userService.loginUser(username, password);
        return ResponseEntity.status(authResponse.getStatusCode()).body(authResponse);
    }
}
