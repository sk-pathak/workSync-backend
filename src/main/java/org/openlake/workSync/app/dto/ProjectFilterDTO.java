package org.openlake.workSync.app.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.openlake.workSync.app.domain.enumeration.ProjectStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectFilterDTO {
    private ProjectStatus status;
    private Boolean ownedByMe;
    private Boolean memberOf;
    private Boolean starred;
} 