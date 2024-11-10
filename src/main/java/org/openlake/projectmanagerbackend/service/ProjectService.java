package org.openlake.projectmanagerbackend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openlake.projectmanagerbackend.domain.ProjectResponse;
import org.openlake.projectmanagerbackend.domain.dto.Project;
import org.openlake.projectmanagerbackend.domain.entity.ProjectEntity;
import org.openlake.projectmanagerbackend.domain.entity.UserEntity;
import org.openlake.projectmanagerbackend.repo.ProjectRepo;
import org.openlake.projectmanagerbackend.repo.UserRepo;
import org.openlake.projectmanagerbackend.utils.Utils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
@Transactional(rollbackOn = Exception.class)
@Slf4j
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepo projectRepo;
    private final UserRepo userRepo;

    public ProjectResponse getAllProjects(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        ProjectResponse projectResponse = new ProjectResponse();
        try{
            Page<ProjectEntity> projectEntities = projectRepo.findAll(pageable);
            List<Project> projects = Utils.mapProjectListEntityToProjectList(projectEntities.getContent());

            long total = projectRepo.count();
            boolean hasMore = projectEntities.hasNext();

            projectResponse.setStatusCode(200);
            projectResponse.setMessage("Success");
            projectResponse.setProjectList(projects);
            projectResponse.setTotalCount(total);
            projectResponse.setTotalPages(projectEntities.getTotalPages());
            projectResponse.setCurrentPage(page);
            projectResponse.setHasMore(hasMore);
        }
        catch (Exception e){
            projectResponse.setStatusCode(500);
            projectResponse.setMessage("Error getting projects: "+e.getMessage());
        }
        return projectResponse;
    }

    public ProjectResponse getProjectById(Long id) {
        ProjectResponse projectResponse = new ProjectResponse();
        try{
            ProjectEntity projectEntity = projectRepo.findById(id).orElseThrow(()-> new RuntimeException("Project not found"));
            Project project = Utils.mapProjectEntitytoProject(projectEntity);
            projectResponse.setStatusCode(200);
            projectResponse.setMessage("Success");
            projectResponse.setProject(project);
        }
        catch (Exception e){
            projectResponse.setStatusCode(500);
            projectResponse.setMessage("Error getting project: "+e.getMessage());
        }
        return projectResponse;
    }

    public ProjectResponse deleteProjectById(Long id) {
        ProjectResponse projectResponse = new ProjectResponse();
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
            projectResponse.setStatusCode(200);
            projectResponse.setMessage("Success, Deleted project "+id);
        }
        catch (Exception e){
            projectResponse.setStatusCode(500);
            projectResponse.setMessage("Error deleting project: "+e.getMessage());
        }
        return projectResponse;
    }

    public ProjectResponse createProject(ProjectEntity projectEntity) {
        ProjectResponse projectResponse = new ProjectResponse();
        try{
            if(projectRepo.existsByProjectName(projectEntity.getProjectName())){
                projectResponse.setStatusCode(400);
                projectResponse.setMessage("Project name already exists");
            }
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            projectEntity.setCreatedBy(username);

            UserEntity userEntity = userRepo.findByUsername(username).orElseThrow(()-> new RuntimeException("User not found"));
            projectEntity.getUserEntities().add(userEntity);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date currentDate = new Date();
            String formattedDate = sdf.format(currentDate);
            projectEntity.setDate(formattedDate);

            ProjectEntity savedProjectEntity = projectRepo.save(projectEntity);
            Project savedProject = Utils.mapProjectEntitytoProject(savedProjectEntity);
            projectResponse.setStatusCode(201);
            projectResponse.setMessage("Success, created project");
            projectResponse.setProject(savedProject);
        }
        catch (Exception e){
            projectResponse.setStatusCode(500);
            projectResponse.setMessage("Error creating project: "+e.getMessage());
        }
        return projectResponse;
    }

    public ProjectResponse addUserToProject(Long projectId) {
        ProjectResponse projectResponse = new ProjectResponse();
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            UserEntity userEntity = userRepo.findByUsername(username).orElseThrow(()-> new RuntimeException("User not logged in"));

            ProjectEntity projectEntity = projectRepo.findById(projectId).orElseThrow(()-> new RuntimeException("Project not found"));
            projectEntity.getUserEntities().add(userEntity);

            ProjectEntity savedProjectEntity = projectRepo.save(projectEntity);
            Project savedProject = Utils.mapProjectEntitytoProject(savedProjectEntity);
            projectResponse.setStatusCode(201);
            projectResponse.setMessage("Success, added user to project");
            projectResponse.setProject(savedProject);
        }
        catch (Exception e){
            projectResponse.setStatusCode(500);
            projectResponse.setMessage("Error updating project: "+e.getMessage());
        }
        return projectResponse;
    }
}
