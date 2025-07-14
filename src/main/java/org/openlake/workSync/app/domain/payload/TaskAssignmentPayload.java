package org.openlake.workSync.app.domain.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskAssignmentPayload {
    private UUID taskId;
    private String taskTitle;
    private String taskDescription;
    private String oldStatus;
    private String newStatus;
    private LocalDate dueDate;
    private Integer priority;
    private String comment;
    private UUID commentId;
}