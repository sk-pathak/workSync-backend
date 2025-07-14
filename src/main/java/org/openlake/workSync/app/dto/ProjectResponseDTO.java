package org.openlake.workSync.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.time.Instant;
import java.util.UUID;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponseDTO {
    private UUID id;
    private String name;
    private String description;
    private String status;
    @JsonProperty("isPublic")
    private boolean isPublic;
    private UUID ownerId;
    private UserResponseDTO owner;
    private Instant createdAt;
    private Instant updatedAt;
    private UUID chatId;
    private Integer memberCount;
    
    private Integer totalTasks;
    private Integer completedTasks;
    private Double progressPercentage;
    private Map<String, Integer> taskStatusBreakdown;
}
