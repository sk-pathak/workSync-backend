package org.openlake.workSync.app.domain.dto;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponseDTO {
    private UUID id;
    private UUID chatId;
    private UUID senderId;
    private String content;
    private Instant sentAt;
}
