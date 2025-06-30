package org.openlake.workSync.app.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageRequestDTO {
    @NotNull
    private UUID chatId;

    @NotNull @Size(min = 1)
    private String content;
}
