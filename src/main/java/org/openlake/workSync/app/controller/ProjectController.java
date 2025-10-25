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
import org.springframework.cache.CacheManager;
import org.openlake.workSync.app.dto.ProjectFilterDTO;
import org.openlake.workSync.app.domain.enumeration.ProjectStatus;

import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;
    private final GithubAnalyticsService githubAnalyticsService;
    private final CacheManager cacheManager;

    @GetMapping
    public ResponseEntity<PagedResponse<ProjectResponseDTO>> listProjects(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) ProjectStatus status,
        @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(projectService.listProjects(search, status, pageable));
    }

    @GetMapping("/filtered")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<ProjectResponseDTO>> listProjectsWithFilters(
        @AuthenticationPrincipal org.openlake.workSync.app.domain.entity.User user,
        @PageableDefault Pageable pageable,
        @RequestParam(required = false) ProjectStatus status,
        @RequestParam(required = false) Boolean ownedByMe,
        @RequestParam(required = false) Boolean memberOf,
        @RequestParam(required = false) Boolean starred
    ) {
        ProjectFilterDTO filter = ProjectFilterDTO.builder()
            .status(status)
            .ownedByMe(ownedByMe)
            .memberOf(memberOf)
            .starred(starred)
            .build();
        
        return ResponseEntity.ok(projectService.listProjectsWithFilters(filter, user.getId(), pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> getProjectById(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> starProject(@AuthenticationPrincipal org.openlake.workSync.app.domain.entity.User user, @PathVariable UUID id) {
        projectService.starProject(user.getId(), id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/unstar")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> unstarProject(@AuthenticationPrincipal org.openlake.workSync.app.domain.entity.User user, @PathVariable UUID id) {
        projectService.unstarProject(user.getId(), id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/join")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> joinProject(@AuthenticationPrincipal org.openlake.workSync.app.domain.entity.User user, @PathVariable UUID id) {
        projectService.requestJoinProject(user.getId(), id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/leave")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> leaveProject(@AuthenticationPrincipal org.openlake.workSync.app.domain.entity.User user, @PathVariable UUID id) {
        projectService.leaveProject(user.getId(), id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/starred")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Boolean>> checkIfStarred(
        @AuthenticationPrincipal org.openlake.workSync.app.domain.entity.User user, 
        @PathVariable UUID id
    ) {
        boolean isStarred = projectService.hasUserStarredProject(user.getId(), id);
        return ResponseEntity.ok(Map.of("starred", isStarred));
    }

    @GetMapping("/{id}/membership")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> checkMembership(
        @AuthenticationPrincipal org.openlake.workSync.app.domain.entity.User user, 
        @PathVariable UUID id
    ) {
        return ResponseEntity.ok(projectService.getMembershipStatus(id, user.getId()));
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

    @GetMapping("/cache-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        
        var projectOwnerCache = cacheManager.getCache("projectOwner");
        var projectAuthCache = cacheManager.getCache("projectAuthorization");
        
        if (projectOwnerCache != null) {
            stats.put("projectOwnerCache", "Cache exists");
        }
        
        if (projectAuthCache != null) {
            stats.put("projectAuthorizationCache", "Cache exists");
        }
        
        return ResponseEntity.ok(stats);
    }
}
