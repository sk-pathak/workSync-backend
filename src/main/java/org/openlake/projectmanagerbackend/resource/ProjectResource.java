package org.openlake.projectmanagerbackend.resource;

import org.openlake.projectmanagerbackend.domain.Project;
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
    public Project getProjectById(@PathVariable long id) {
        return projectService.getProjectById(id);
    }

    @DeleteMapping("/all/{id}")
    public void deleteProjectById(@PathVariable long id) {
        projectService.deleteProjectById(id);
    }

    @PostMapping("/add")
    public Project addProject(@RequestBody Project project) {
        return projectService.createProject(project);
    }
}
