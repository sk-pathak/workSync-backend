package org.openlake.workSync.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskRequestDTO {
    @NotBlank @Size(max = 255)
    private String title;

    private String description;
    private LocalDate dueDate;
    private Integer priority;
    private String status;
    private UUID assigneeId;
}
