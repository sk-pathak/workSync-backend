package org.openlake.workSync.app.controller;

import lombok.RequiredArgsConstructor;
import org.openlake.workSync.app.dto.UserRequestDTO;
import org.openlake.workSync.app.dto.UserResponseDTO;
import org.openlake.workSync.app.service.UserService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.openlake.workSync.app.dto.PagedResponse;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDTO> getCurrentUser(@AuthenticationPrincipal org.openlake.workSync.app.domain.entity.User user) {
        return ResponseEntity.ok(userService.getUserById(user.getId()));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> updateCurrentUser(@AuthenticationPrincipal org.openlake.workSync.app.domain.entity.User user, @RequestBody UserRequestDTO request) {
        return ResponseEntity.ok(userService.updateUser(user.getId(), request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<UserResponseDTO>> listUsers(@PageableDefault Pageable pageable) {
        return ResponseEntity.ok(userService.listUsers(pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable UUID id, @RequestBody UserRequestDTO request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/projects/owned")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getOwnedProjects(@AuthenticationPrincipal org.openlake.workSync.app.domain.entity.User user, @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(userService.getOwnedProjects(user.getId(), pageable));
    }

    @GetMapping("/me/projects/joined")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getJoinedProjects(@AuthenticationPrincipal org.openlake.workSync.app.domain.entity.User user, @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(userService.getJoinedProjects(user.getId(), pageable));
    }

    @GetMapping("/me/projects/starred")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getStarredProjects(@AuthenticationPrincipal org.openlake.workSync.app.domain.entity.User user, @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(userService.getStarredProjects(user.getId(), pageable));
    }
}
