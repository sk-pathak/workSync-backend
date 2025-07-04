package org.openlake.workSync.app.domain.dto;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponseDTO {
    private UUID id;
    private String name;
    private String description;
    private String status;
    private boolean isPublic;
    private UUID ownerId;
    private Instant createdAt;
    private Instant updatedAt;
}
