package org.openlake.workSync.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.openlake.workSync.app.domain.enumeration.ProjectStatus;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Project {
    private Long projectId;
    private String projectName;
    private String projectDescription;
    private String createdBy;
    private String sourceCodeLink;
    private String projectImageLink;
    private ProjectStatus projectStatus;
    private String date;
    private int stars;
    private List<String> tags;
    private List<ProjectLink> projectLinks;
    private List<User> starredBy;
    private List<User> users;
}
