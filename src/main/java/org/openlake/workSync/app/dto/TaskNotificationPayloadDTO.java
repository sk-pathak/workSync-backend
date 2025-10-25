package org.openlake.workSync.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openlake.workSync.app.domain.enumeration.Priority;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskNotificationPayloadDTO {
    private UUID taskId;
    private String taskTitle;
    private String taskDescription;
    private String oldStatus;
    private String newStatus;
    private LocalDate dueDate;
    private Priority priority;
    private String comment;
    private UUID commentId;
}
