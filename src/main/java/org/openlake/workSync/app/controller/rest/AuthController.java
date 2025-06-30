package org.openlake.workSync.app.controller.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.openlake.workSync.app.domain.dto.AuthResponse;
import org.openlake.workSync.app.domain.entity.UserEntity;
import org.openlake.workSync.app.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestPart("user") @Valid UserEntity userEntity,
                                                 @RequestPart(value = "image", required = false) MultipartFile image) {
        AuthResponse authResponse = userService.createUser(userEntity, image);
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
