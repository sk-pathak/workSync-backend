package org.openlake.workSync.app.mapper;

import org.mapstruct.*;
import org.openlake.workSync.app.domain.entity.Message;
import org.openlake.workSync.app.dto.MessageRequestDTO;
import org.openlake.workSync.app.dto.MessageResponseDTO;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    @Mapping(target = "chatId", source = "chat.id")
    @Mapping(target = "senderId", source = "sender.id")
    MessageResponseDTO toResponse(Message entity);

    default MessageResponseDTO toResponseDTO(Message entity) {
        return toResponse(entity);
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chat", ignore = true)
    @Mapping(target = "sender", ignore = true)
    @Mapping(target = "sentAt", ignore = true)
    Message toEntity(MessageRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "chat", ignore = true)
    @Mapping(target = "sender", ignore = true)
    @Mapping(target = "sentAt", ignore = true)
    void updateEntityFromDTO(MessageRequestDTO dto, @MappingTarget Message entity);
}
