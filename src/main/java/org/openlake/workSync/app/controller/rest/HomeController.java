package org.openlake.workSync.app.controller.rest;

import org.openlake.workSync.app.domain.dto.AuthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping({"/", "/home"})
    public ResponseEntity<AuthResponse> home() {
        AuthResponse authResponse = new AuthResponse();
        authResponse.setStatusCode(200);
        authResponse.setMessage("Success");
        return ResponseEntity.status(authResponse.getStatusCode()).body(authResponse);
    }
}
