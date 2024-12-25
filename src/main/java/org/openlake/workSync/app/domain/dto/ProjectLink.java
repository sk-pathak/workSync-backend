package org.openlake.workSync.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectLink {
    private Long linkId;
    private String linkUrl;
    private String linkName;
    private String linkDesc;
}
