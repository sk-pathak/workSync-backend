package org.openlake.workSync.app.utils;

import org.openlake.workSync.app.domain.dto.Project;
import org.openlake.workSync.app.domain.dto.ProjectLink;
import org.openlake.workSync.app.domain.dto.User;
import org.openlake.workSync.app.domain.entity.ProjectEntity;
import org.openlake.workSync.app.domain.entity.ProjectLinkEntity;
import org.openlake.workSync.app.domain.entity.UserEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {

    public static User mapUserEntitytoUser(UserEntity userEntity) {
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
        project.setUsers(projectEntity.getUserEntities().stream().map(Utils::mapUserEntitytoUser).collect(Collectors.toList()));
        project.setStarredBy(projectEntity.getUserStarredEntities().stream().map(Utils::mapUserEntitytoUser).collect(Collectors.toList()));
        project.setProjectLinks(projectEntity.getProjectLinkEntities().stream().map(Utils::mapProjectLinkEntitytoProjectLink).collect(Collectors.toList()));
        return project;
    }

    public static List<User> mapUserListEntityToUserList(List<UserEntity> userListEntity) {
        return userListEntity.stream().map(Utils::mapUserEntitytoUser).collect(Collectors.toList());
    }

    public static List<Project> mapProjectListEntityToProjectList(List<ProjectEntity> projectListEntity) {
        return projectListEntity.stream().map(Utils::mapProjectEntitytoProject).collect(Collectors.toList());
    }

    public static String saveImage(MultipartFile image) throws IOException {
        String UPLOAD_DIR = "D:\\Programming\\Projects\\workSync\\workSync-frontend\\uploads";
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(image.getInputStream(), filePath);

        // to set local path from frontend in database
        uploadPath = Paths.get("\\uploads");
        filePath = uploadPath.resolve(fileName);
        return filePath.toString();
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
