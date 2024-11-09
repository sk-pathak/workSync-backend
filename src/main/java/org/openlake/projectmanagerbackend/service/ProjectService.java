package org.openlake.projectmanagerbackend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openlake.projectmanagerbackend.domain.Response;
import org.openlake.projectmanagerbackend.domain.dto.Project;
import org.openlake.projectmanagerbackend.domain.entity.ProjectEntity;
import org.openlake.projectmanagerbackend.repo.ProjectRepo;
import org.openlake.projectmanagerbackend.utils.Utils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional(rollbackOn = Exception.class)
@Slf4j
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepo projectRepo;

    public Response getAllProjects() {
        Response response = new Response();
        try{
            List<ProjectEntity> projectEntities = projectRepo.findAll();
            List<Project> projects = Utils.mapProjectListEntityToProjectList(projectEntities);
            response.setStatusCode(200);
            response.setMessage("Success");
            response.setProjectList(projects);
        }
        catch (Exception e){
            response.setStatusCode(500);
            response.setMessage("Error getting projects: "+e.getMessage());
        }
        return response;
    }

    public Response getProjectById(Long id) {
        Response response = new Response();
        try{
            ProjectEntity projectEntity = projectRepo.findById(id).orElseThrow(()-> new RuntimeException("Project not found"));
            Project project = Utils.mapProjectEntitytoProject(projectEntity);
            response.setStatusCode(200);
            response.setMessage("Success");
            response.setProject(project);
        }
        catch (Exception e){
            response.setStatusCode(500);
            response.setMessage("Error getting project: "+e.getMessage());
        }
        return response;
    }

    public Response deleteProjectById(Long id) {
        Response response = new Response();
        try{
            ProjectEntity projectEntity = projectRepo.findById(id).orElseThrow(()-> new RuntimeException("Project not found"));
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            if(projectEntity.getCreatedBy().equals(username)){
                projectRepo.delete(projectEntity);
            }
            else{
                throw new RuntimeException("User not logged in");
            }
            response.setStatusCode(200);
            response.setMessage("Success, Deleted project "+id);
        }
        catch (Exception e){
            response.setStatusCode(500);
            response.setMessage("Error deleting project: "+e.getMessage());
        }
        return response;
    }

    public Response createProject(ProjectEntity projectEntity) {
        Response response = new Response();
        try{
            if(projectRepo.existsByProjectName(projectEntity.getProjectName())){
                response.setStatusCode(400);
                response.setMessage("Project name already exists");
            }
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            projectEntity.setCreatedBy(username);

            ProjectEntity savedProjectEntity = projectRepo.save(projectEntity);
            Project savedProject = Utils.mapProjectEntitytoProject(savedProjectEntity);
            response.setStatusCode(201);
            response.setMessage("Success, created project");
            response.setProject(savedProject);
        }
        catch (Exception e){
            response.setStatusCode(500);
            response.setMessage("Error creating project: "+e.getMessage());
        }
        return response;
    }
}
