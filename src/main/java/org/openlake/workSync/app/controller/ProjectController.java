package org.openlake.workSync.app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.openlake.workSync.app.domain.ProjectResponse;
import org.openlake.workSync.app.domain.entity.ProjectEntity;
import org.openlake.workSync.app.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping("/all")
    public ResponseEntity<ProjectResponse> getAllProjects(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String order,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        ProjectResponse projectResponse = projectService.getAllProjects(searchTerm, sortBy, order, page, size);
        return ResponseEntity.status(projectResponse.getStatusCode()).body(projectResponse);
    }

    @GetMapping("/all/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long id) {
        ProjectResponse projectResponse = projectService.getProjectById(id);
        return ResponseEntity.status(projectResponse.getStatusCode()).body(projectResponse);
    }

    @DeleteMapping("/all/{id}")
    public ResponseEntity<ProjectResponse> deleteProjectById(@PathVariable Long id) {
        ProjectResponse projectResponse = projectService.deleteProjectById(id);
        return ResponseEntity.status(projectResponse.getStatusCode()).body(projectResponse);
    }

    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    public ResponseEntity<ProjectResponse> createProject(@RequestPart("project") @Valid ProjectEntity projectEntity,
                                                         @RequestPart(value = "image", required = false) MultipartFile image) {
        ProjectResponse projectResponse = projectService.createProject(projectEntity, image);
        return ResponseEntity.status(projectResponse.getStatusCode()).body(projectResponse);
    }

    @PutMapping("/adduser/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable Long projectId) {
        ProjectResponse projectResponse = projectService.addUserToProject(projectId);
        return ResponseEntity.status(projectResponse.getStatusCode()).body(projectResponse);
    }

    @PutMapping("/star/{projectId}")
    public ResponseEntity<ProjectResponse> starProject(@PathVariable Long projectId) {
        ProjectResponse projectResponse = projectService.starProject(projectId);
        return ResponseEntity.status(projectResponse.getStatusCode()).body(projectResponse);
    }

    @PutMapping("/update/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable Long projectId, @RequestBody ProjectEntity projectEntity) {
        ProjectResponse projectResponse = projectService.updateProject(projectId, projectEntity);
        return ResponseEntity.status(projectResponse.getStatusCode()).body(projectResponse);
    }
}
