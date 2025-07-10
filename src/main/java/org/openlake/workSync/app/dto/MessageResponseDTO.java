package org.openlake.workSync.app.dto;

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
    private String senderUsername;
    private String senderName;
    private String senderAvatarUrl;
    private String content;
    private Instant sentAt;
}
