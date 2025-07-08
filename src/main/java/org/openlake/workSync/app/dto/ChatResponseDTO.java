package org.openlake.workSync.app.dto;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponseDTO {
    private UUID id;
    private UUID projectId;
    private String name;
    private Instant createdAt;
    private Instant updatedAt;
}
