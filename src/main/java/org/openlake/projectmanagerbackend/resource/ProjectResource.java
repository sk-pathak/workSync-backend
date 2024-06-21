package org.openlake.projectmanagerbackend.resource;

import jakarta.validation.Valid;
import org.openlake.projectmanagerbackend.domain.entities.Project;
import org.openlake.projectmanagerbackend.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectResource {
    @Autowired
    private ProjectService projectService;

    @GetMapping("/all")
    public List<Project> getAllProjects() {
        return projectService.getAllProjects();
    }

    @GetMapping("/all/{id}")
    public Project getProjectById(@PathVariable @Valid Long id) {
        return projectService.getProjectById(id);
    }

    @DeleteMapping("/all/{id}")
    public void deleteProjectById(@PathVariable @Valid Long id) {
        projectService.deleteProjectById(id);
    }

    @PostMapping("/create")
    public Project createProject(@RequestBody @Valid Project project) {
        System.out.println("in resource");
        return projectService.createProject(project);
    }
}