package org.openlake.projectmanagerbackend.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Project {
    private Long projectId;
    private String projectName;
    private String projectDescription;
    private String createdBy;
    private String sourceCodeLink;
    private List<String> tags;
    private List<User> users;
}
