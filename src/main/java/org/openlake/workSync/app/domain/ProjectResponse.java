package org.openlake.workSync.app.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.openlake.workSync.app.domain.dto.Project;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectResponse extends Response {
    private Long totalCount;
    private int totalPages;
    private int currentPage;
    private Boolean hasMore;

    private Project project;
    private List<Project> projectList;
}
