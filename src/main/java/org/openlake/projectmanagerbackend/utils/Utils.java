package org.openlake.projectmanagerbackend.utils;

import org.openlake.projectmanagerbackend.domain.dto.Project;
import org.openlake.projectmanagerbackend.domain.dto.User;
import org.openlake.projectmanagerbackend.domain.entity.ProjectEntity;
import org.openlake.projectmanagerbackend.domain.entity.UserEntity;

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
        return user;
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
        project.setTags(projectEntity.getTags());
        project.setStars(projectEntity.getStars());
        return project;
    }

    public static List<User> mapUserListEntityToUserList(List<UserEntity> userListEntity) {
        return userListEntity.stream().map(Utils::mapUserEntitytoUser).collect(Collectors.toList());
    }

    public static List<Project> mapProjectListEntityToProjectList(List<ProjectEntity> projectListEntity) {
        return projectListEntity.stream().map(Utils::mapProjectEntitytoProject).collect(Collectors.toList());
    }
}
