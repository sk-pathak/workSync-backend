package org.openlake.workSync.app.utils;

import org.openlake.workSync.app.domain.dto.Project;
import org.openlake.workSync.app.domain.dto.User;
import org.openlake.workSync.app.domain.entity.ProjectEntity;
import org.openlake.workSync.app.domain.entity.UserEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {

    public static User mapUserEntitytoUser(User user) {
        User user = new User();
        user.setUserId(userEntity.getUserId());
        user.setName(userEntity.getName());
        user.setEmail(userEntity.getEmail());
        user.setPassword(userEntity.getPassword());
        user.setRole(userEntity.getRole());
        user.setUserProfile(userEntity.getUserProfile());
        return user;
    }

    private static ProjectLink mapProjectLinkEntitytoProjectLink(ProjectLinkEntity projectLinkEntity) {
        ProjectLink projectLink = new ProjectLink();
        projectLink.setLinkId(projectLinkEntity.getLinkId());
        projectLink.setLinkName(projectLinkEntity.getLinkName());
        projectLink.setLinkUrl(projectLinkEntity.getLinkUrl());
        projectLink.setLinkDesc(projectLinkEntity.getLinkDesc());
        return projectLink;
    }

    public static Project mapProjectEntitytoProject(ProjectEntity projectEntity) {
        if (projectEntity == null) {
            throw new IllegalArgumentException("ProjectEntity cannot be null");
        }

        Project project = new Project();

        project.setProjectId(projectEntity.getProjectId());
        project.setProjectName(projectEntity.getProjectName());
        project.setProjectDescription(projectEntity.getProjectDescription());
        project.setCreatedBy(projectEntity.getCreatedBy());
        project.setDate(projectEntity.getDate());
        project.setProjectImageLink(projectEntity.getProjectImageLink());
        project.setSourceCodeLink(projectEntity.getSourceCodeLink());
        project.setProjectStatus(projectEntity.getProjectStatus());
        project.setTags(projectEntity.getTags());
        project.setStars(projectEntity.getStars());

        if (projectEntity.getUserEntities() != null && !projectEntity.getUserEntities().isEmpty()) {
            project.setUsers(projectEntity.getUserEntities().stream()
                    .map(Utils::mapUserEntitytoUser)
                    .collect(Collectors.toList()));
        } else {
            project.setUsers(new ArrayList<>());
        }

        if (projectEntity.getUserStarredEntities() != null && !projectEntity.getUserStarredEntities().isEmpty()) {
            project.setStarredBy(projectEntity.getUserStarredEntities().stream()
                    .map(Utils::mapUserEntitytoUser)
                    .collect(Collectors.toList()));
        } else {
            project.setStarredBy(new ArrayList<>());
        }

        if (projectEntity.getProjectLinkEntities() != null && !projectEntity.getProjectLinkEntities().isEmpty()) {
            project.setProjectLinks(projectEntity.getProjectLinkEntities().stream()
                    .map(Utils::mapProjectLinkEntitytoProjectLink)
                    .collect(Collectors.toList()));
        } else {
            project.setProjectLinks(new ArrayList<>());
        }

        return project;
    }


    public static List<User> mapUserListEntityToUserList(List<UserEntity> userListEntity) {
        return userListEntity.stream().map(Utils::mapUserEntitytoUser).collect(Collectors.toList());
    }

    public static List<Project> mapProjectListEntityToProjectList(List<ProjectEntity> projectListEntity) {
        return projectListEntity.stream().map(Utils::mapProjectEntitytoProject).collect(Collectors.toList());
    }

    public static String mapSortField(String sortBy) {
        return switch (sortBy) {
            case "name" -> "projectName";
            case "date" -> "date";
            case "stars" -> "stars";
            default -> throw new IllegalArgumentException("Invalid sort field: " + sortBy);
        };
    }
}
