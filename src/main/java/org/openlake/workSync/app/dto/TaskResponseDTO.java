package org.openlake.workSync.app.dto;

import lombok.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponseDTO {
    private UUID id;
    private UUID projectId;
    private UUID creatorId;
    private UUID assigneeId;
    private UserResponseDTO assignee;
    private String title;
    private String description;
    private String status;
    private LocalDate dueDate;
    private Integer priority;
    private Instant createdAt;
    private Instant updatedAt;
}
