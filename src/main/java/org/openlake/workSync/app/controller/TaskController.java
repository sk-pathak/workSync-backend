package org.openlake.workSync.app.controller;

import lombok.RequiredArgsConstructor;
import org.openlake.workSync.app.dto.TaskRequestDTO;
import org.openlake.workSync.app.dto.TaskResponseDTO;
import org.openlake.workSync.app.service.TaskService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.openlake.workSync.app.dto.PagedResponse;

import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @GetMapping
    @PreAuthorize("@projectService.isMemberOrOwnerOrAdmin(#projectId, principal)")
    public ResponseEntity<PagedResponse<TaskResponseDTO>> listTasks(@PathVariable UUID projectId, @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(taskService.listTasks(projectId, pageable));
    }

    @PostMapping
    @PreAuthorize("@projectService.isMemberOrOwnerOrAdmin(#projectId, principal)")
    public ResponseEntity<TaskResponseDTO> createTask(@AuthenticationPrincipal org.openlake.workSync.app.domain.entity.User user, @PathVariable UUID projectId, @RequestBody TaskRequestDTO request) {
        return ResponseEntity.ok(taskService.createTask(user.getId(), projectId, request));
    }

    @PutMapping("/{taskId}")
    @PreAuthorize("@projectService.isMemberOrOwnerOrAdmin(#projectId, principal)")
    public ResponseEntity<TaskResponseDTO> updateTask(@PathVariable UUID projectId, @PathVariable UUID taskId, @RequestBody TaskRequestDTO request) {
        return ResponseEntity.ok(taskService.updateTask(projectId, taskId, request));
    }

    @DeleteMapping("/{taskId}")
    @PreAuthorize("@projectService.isOwnerOrAdmin(#projectId, principal)")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID projectId, @PathVariable UUID taskId) {
        taskService.deleteTask(projectId, taskId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{taskId}/assign/{userId}")
    @PreAuthorize("@projectService.isOwnerOrAdmin(#projectId, principal)")
    public ResponseEntity<TaskResponseDTO> assignTask(@PathVariable UUID projectId, @PathVariable UUID taskId, @PathVariable UUID userId) {
        return ResponseEntity.ok(taskService.assignTask(projectId, taskId, userId));
    }

    @PostMapping("/{taskId}/status")
    @PreAuthorize("@projectService.isMemberOrOwnerOrAdmin(#projectId, principal)")
    public ResponseEntity<TaskResponseDTO> updateTaskStatus(@PathVariable UUID projectId, @PathVariable UUID taskId, @RequestParam String status) {
        return ResponseEntity.ok(taskService.updateTaskStatus(projectId, taskId, status));
    }
}
