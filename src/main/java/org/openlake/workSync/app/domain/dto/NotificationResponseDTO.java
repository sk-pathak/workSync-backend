package org.openlake.workSync.app.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDTO {
    private UUID id;
    private UUID recipientId;
    private UUID senderId;
    private UUID projectId;
    private String type;
    private String status;
    private String payload;
    private Instant createdAt;
    private Instant updatedAt;
}
