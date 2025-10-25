package org.openlake.workSync.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatEventDTO {
    
    private UUID eventId;

    private ChatEventType eventType;
    
    private Instant timestamp;
    
    private UUID chatId;
    
    private UUID userId;
    
    private String username;
    private String userDisplayName;
    private String userAvatarUrl;
    
    private String payload;
    
    private String metadata;
    
    public enum ChatEventType {
        MESSAGE_SENT,
        MESSAGE_EDITED,
        MESSAGE_DELETED,
        USER_JOINED,
        USER_LEFT,
        TYPING_STARTED,
        TYPING_STOPPED
    }
}
