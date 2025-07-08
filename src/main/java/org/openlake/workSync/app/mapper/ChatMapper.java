package org.openlake.workSync.app.mapper;

import org.mapstruct.*;
import org.openlake.workSync.app.domain.entity.Chat;
import org.openlake.workSync.app.dto.ChatRequestDTO;
import org.openlake.workSync.app.dto.ChatResponseDTO;

@Mapper(componentModel = "spring")
public interface ChatMapper {
    @Mapping(target = "projectId", source = "project.id")
    ChatResponseDTO toResponse(Chat entity);

    default ChatResponseDTO toResponseDTO(Chat entity) {
        return toResponse(entity);
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Chat toEntity(ChatRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(ChatRequestDTO dto, @MappingTarget Chat entity);
}
