package org.openlake.workSync.app.domain.dto;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {
    private UUID id;
    private String username;
    private String email;
    private String name;
    private String bio;
    private String avatarUrl;
    private String role;
    private Instant createdAt;
    private Instant updatedAt;
}
