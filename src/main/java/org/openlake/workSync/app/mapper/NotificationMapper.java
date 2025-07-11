package org.openlake.workSync.app.mapper;

import org.mapstruct.*;
import org.openlake.workSync.app.domain.entity.Notification;
import org.openlake.workSync.app.dto.NotificationResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    @Mapping(target = "recipientId", source = "recipient.id")
    @Mapping(target = "senderId", source = "sender.id")
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "payload", source = "payload", qualifiedByName = "objectToString")
    NotificationResponseDTO toResponse(Notification entity);

    default NotificationResponseDTO toResponseDTO(Notification entity) {
        return toResponse(entity);
    }

    @Named("objectToString")
    default String objectToString(Object payload) {
        if (payload == null) {
            return null;
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return payload.toString();
        }
    }
}
