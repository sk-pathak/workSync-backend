package org.openlake.projectmanagerbackend.resource;

import lombok.RequiredArgsConstructor;
import org.openlake.projectmanagerbackend.domain.Response;
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
    public ResponseEntity<Response> register(@RequestBody UserEntity userEntity) {
        Response response = userService.createUser(userEntity);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Response> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        Response response = userService.loginUser(username, password);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
