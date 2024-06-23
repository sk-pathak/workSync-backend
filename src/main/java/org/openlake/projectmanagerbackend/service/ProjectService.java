package org.openlake.projectmanagerbackend.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openlake.projectmanagerbackend.domain.entity.ProjectEntity;
import org.openlake.projectmanagerbackend.repo.ProjectRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional(rollbackOn = Exception.class)
@Slf4j
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepo projectRepo;

    public List<ProjectEntity> getAllProjects() {
        return projectRepo.findAll();
    }

    public ProjectEntity getProjectById(Long id) {
        return projectRepo.findById(id).orElse(null);
    }

    public void deleteProjectById(Long id) {
        projectRepo.deleteById(id);
    }

    public ProjectEntity createProject(ProjectEntity projectEntity) {
        // ADD USER LIST (current user in the list)
        return projectRepo.save(projectEntity);
    }
}
