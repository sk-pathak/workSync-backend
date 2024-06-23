package org.openlake.projectmanagerbackend.resource;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.openlake.projectmanagerbackend.domain.entity.ProjectEntity;
import org.openlake.projectmanagerbackend.service.ProjectService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/projects")
public class ProjectResource {

    private final ProjectService projectService;

    @GetMapping("/all")
    public List<ProjectEntity> getAllProjects() {
        return projectService.getAllProjects();
    }

    @GetMapping("/all/{id}")
    public ProjectEntity getProjectById(@PathVariable @Valid Long id) {
        return projectService.getProjectById(id);
    }

    @DeleteMapping("/all/{id}")
    public void deleteProjectById(@PathVariable @Valid Long id) {
        projectService.deleteProjectById(id);
    }

    @PostMapping("/create")
    public ProjectEntity createProject(@RequestBody @Valid ProjectEntity projectEntity) {
        System.out.println("in resource");
        return projectService.createProject(projectEntity);
    }
}
