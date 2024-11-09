package org.openlake.projectmanagerbackend.resource;

import lombok.RequiredArgsConstructor;
import org.openlake.projectmanagerbackend.domain.Response;
import org.openlake.projectmanagerbackend.domain.entity.ProjectEntity;
import org.openlake.projectmanagerbackend.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/projects")
public class ProjectResource {

    private final ProjectService projectService;

    @GetMapping("/all")
    public ResponseEntity<Response> getAllProjects() {
        Response response = projectService.getAllProjects();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/all/{id}")
    public ResponseEntity<Response> getProjectById(@PathVariable Long id) {
        Response response = projectService.getProjectById(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/all/{id}")
    public ResponseEntity<Response> deleteProjectById(@PathVariable Long id) {
        Response response = projectService.deleteProjectById(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/create")
    public ResponseEntity<Response> createProject(@RequestBody ProjectEntity projectEntity) {
        Response response = projectService.createProject(projectEntity);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
