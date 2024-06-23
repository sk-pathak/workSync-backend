package org.openlake.projectmanagerbackend.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.openlake.projectmanagerbackend.domain.dto.Project;
import org.openlake.projectmanagerbackend.domain.dto.User;
import org.openlake.projectmanagerbackend.domain.entity.ProjectEntity;
import org.openlake.projectmanagerbackend.domain.entity.UserEntity;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {
    private int statusCode;
    private String message;

    private String token;
    private String role;
    private String expirationTime;

    private User user;
    private Project project;
    private List<User> userList;
    private List<Project> projectList;
}
