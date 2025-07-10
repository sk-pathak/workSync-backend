package org.openlake.workSync.app.controller;

import lombok.RequiredArgsConstructor;
import org.openlake.workSync.app.dto.ProjectRequestDTO;
import org.openlake.workSync.app.dto.ProjectResponseDTO;
import org.openlake.workSync.app.service.ProjectService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.openlake.workSync.app.dto.PagedResponse;
import org.openlake.workSync.app.dto.GithubAnalyticsDTO;
import org.openlake.workSync.app.service.GithubAnalyticsService;

import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;
    private final GithubAnalyticsService githubAnalyticsService;

    @GetMapping
    public ResponseEntity<PagedResponse<ProjectResponseDTO>> listProjects(@PageableDefault Pageable pageable) {
        return ResponseEntity.ok(projectService.listProjects(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> getProjectById(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ProjectResponseDTO> createProject(@AuthenticationPrincipal org.openlake.workSync.app.domain.entity.User user, @RequestBody ProjectRequestDTO request) {
        return ResponseEntity.ok(projectService.createProject(user.getId(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@projectService.isOwnerOrAdmin(#id, principal)")
    public ResponseEntity<ProjectResponseDTO> updateProject(@PathVariable UUID id, @RequestBody ProjectRequestDTO request) {
        return ResponseEntity.ok(projectService.updateProject(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@projectService.isOwnerOrAdmin(#id, principal)")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/star")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> starProject(@AuthenticationPrincipal org.openlake.workSync.app.domain.entity.User user, @PathVariable UUID id) {
        projectService.starProject(user.getId(), id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/unstar")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> unstarProject(@AuthenticationPrincipal org.openlake.workSync.app.domain.entity.User user, @PathVariable UUID id) {
        projectService.unstarProject(user.getId(), id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/join")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> joinProject(@AuthenticationPrincipal org.openlake.workSync.app.domain.entity.User user, @PathVariable UUID id) {
        projectService.requestJoinProject(user.getId(), id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/members")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<?>> listMembers(@PathVariable UUID id, @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(projectService.listMembers(id, pageable));
    }

    @PostMapping("/{id}/members/{userId}/approve")
    @PreAuthorize("@projectService.isOwnerOrAdmin(#id, principal)")
    public ResponseEntity<Void> approveMember(@PathVariable UUID id, @PathVariable UUID userId) {
        projectService.approveMember(id, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/members/{userId}/remove")
    @PreAuthorize("@projectService.isOwnerOrAdmin(#id, principal)")
    public ResponseEntity<Void> removeMember(@PathVariable UUID id, @PathVariable UUID userId) {
        projectService.removeMember(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/github-analytics")
    public ResponseEntity<GithubAnalyticsDTO> getGithubAnalytics(@PathVariable UUID id, @RequestParam String repoUrl) {
        return ResponseEntity.ok(githubAnalyticsService.fetchAnalytics(repoUrl));
    }
}
