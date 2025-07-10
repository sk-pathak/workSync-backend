package org.openlake.workSync.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.openlake.workSync.app.domain.enumeration.ProjectStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectRequestDTO {
    @NotBlank @Size(max = 255)
    private String name;

    @NotBlank
    private String description;

    @Builder.Default
    @NotNull
    private boolean isPublic = false;
}
