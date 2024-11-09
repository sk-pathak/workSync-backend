package org.openlake.projectmanagerbackend.resource;

import lombok.RequiredArgsConstructor;
import org.openlake.projectmanagerbackend.domain.Response;
import org.openlake.projectmanagerbackend.domain.dto.User;
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
    public ResponseEntity<Response> getAllUsers() {
        Response response = userService.getAllUsers();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/all/{username}")
    public ResponseEntity<Response> getUserById(@PathVariable("username") String username) {
        Response response = userService.getUserByUsername(username);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/all/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> deleteUser(@PathVariable("username") String username) {
        Response response = userService.deleteUser(username);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
