package org.openlake.workSync.app.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openlake.workSync.app.domain.ProjectResponse;
import org.openlake.workSync.app.domain.dto.Project;
import org.openlake.workSync.app.domain.entity.ProjectEntity;
import org.openlake.workSync.app.domain.entity.UserEntity;
import org.openlake.workSync.app.repo.ProjectRepo;
import org.openlake.workSync.app.repo.UserRepo;
import org.openlake.workSync.app.utils.Utils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@Transactional(rollbackOn = Exception.class)
@Slf4j
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepo projectRepo;
    private final UserRepo userRepo;

    public ProjectResponse getAllProjects(String searchTerm, String sortBy, String order, int page, int size) {
        ProjectResponse projectResponse = new ProjectResponse();
        Pageable pageable = PageRequest.of(page, size);
        Page<ProjectEntity> projectEntities;
        try{
            if(sortBy != null && !sortBy.isEmpty()) {
                List<String> allowedSortFields = Arrays.asList("name", "date", "stars");
                if (!allowedSortFields.contains(sortBy)) {
                    projectResponse.setStatusCode(400);
                    projectResponse.setMessage("Invalid sort field: "+sortBy);
                    return projectResponse;
                }
                String actualSortField = Utils.mapSortField(sortBy);
                if(order != null && !order.isEmpty()) {
                    Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
                    pageable = PageRequest.of(page, size, direction, actualSortField);
                }
                else {
                    pageable = PageRequest.of(page, size, Sort.Direction.ASC,actualSortField);
                }
            }
            if(searchTerm != null && !searchTerm.isEmpty()) {
                if(sortBy!=null && sortBy.equals("name")){
                    Sort.Direction direction = Sort.Direction.ASC;
                    if(order != null && !order.isEmpty()) {
                        direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
                    }
                    pageable = PageRequest.of(page, size, direction, "project_name");
                }
                projectEntities = projectRepo.searchKey(searchTerm, pageable);
            }
            else {
                projectEntities = projectRepo.findAll(pageable);
            }

            List<Project> projects = Utils.mapProjectListEntityToProjectList(projectEntities.getContent());

            long total = projectEntities.getTotalElements();
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

    public ProjectResponse createProject(ProjectEntity projectEntity, MultipartFile image) {
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

            if(image!=null){
                try {
                    if(image.getSize() > 5*1024*1024){
                        projectResponse.setStatusCode(400);
                        projectResponse.setMessage("Image too large");
                        return projectResponse;
                    }
                    String imagePath = Utils.saveImage(image);
                    projectEntity.setProjectImageLink(imagePath);
                }
                catch (IOException e){
                    projectResponse.setStatusCode(400);
                    projectResponse.setMessage(e.getMessage());
                    return projectResponse;
                }
            }

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
            if(projectEntity.getUserEntities().contains(userEntity)){
                projectResponse.setStatusCode(400);
                projectResponse.setMessage("User already exists");
                return projectResponse;
            }
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

    public ProjectResponse updateProject(Long projectId, ProjectEntity projectEntity) {
        ProjectResponse projectResponse = new ProjectResponse();
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            if(projectEntity.getCreatedBy().equals(username)) {
                ProjectEntity projectEntity1 = projectRepo.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));
                projectEntity1.setProjectName(projectEntity.getProjectName());
                projectEntity1.setProjectDescription(projectEntity.getProjectDescription());
                projectEntity1.setProjectStatus(projectEntity.getProjectStatus());
                projectEntity1.setProjectImageLink(projectEntity.getProjectImageLink());
                projectEntity1.setSourceCodeLink(projectEntity.getSourceCodeLink());
                projectEntity1.setStars(projectEntity.getStars());
                projectEntity1.setTags(projectEntity.getTags());
                projectEntity1.setProjectLinkEntities(projectEntity.getProjectLinkEntities());

                ProjectEntity savedProjectEntity = projectRepo.save(projectEntity1);
                Project savedProject = Utils.mapProjectEntitytoProject(savedProjectEntity);
                projectResponse.setStatusCode(201);
                projectResponse.setMessage("Success, updated project");
                projectResponse.setProject(savedProject);
            }
            else {
                projectResponse.setStatusCode(400);
                projectResponse.setMessage("Not authorized to update project");
            }
        }
        catch (Exception e){
            projectResponse.setStatusCode(500);
            projectResponse.setMessage("Error updating project: "+e.getMessage());
        }
        return projectResponse;
    }
}
