package org.openlake.projectmanagerbackend.resource;

import lombok.RequiredArgsConstructor;
import org.openlake.projectmanagerbackend.domain.ProjectResponse;
import org.openlake.projectmanagerbackend.domain.entity.ProjectEntity;
import org.openlake.projectmanagerbackend.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
public class ProjectResource {

    private final ProjectService projectService;

    @GetMapping("/all")
    public ResponseEntity<ProjectResponse> getAllProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        ProjectResponse projectResponse = projectService.getAllProjects(page, size);
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

    @PostMapping("/create")
    public ResponseEntity<ProjectResponse> createProject(@RequestBody ProjectEntity projectEntity) {
        ProjectResponse projectResponse = projectService.createProject(projectEntity);
        return ResponseEntity.status(projectResponse.getStatusCode()).body(projectResponse);
    }

    @PutMapping("/adduser/{projectId}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable Long projectId) {
        ProjectResponse projectResponse = projectService.addUserToProject(projectId);
        return ResponseEntity.status(projectResponse.getStatusCode()).body(projectResponse);
    }
}
