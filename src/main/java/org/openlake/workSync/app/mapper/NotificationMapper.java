package org.openlake.workSync.app.mapper;

import org.mapstruct.*;
import org.openlake.workSync.app.domain.entity.Notification;
import org.openlake.workSync.app.dto.NotificationResponseDTO;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    @Mapping(target = "recipientId", source = "recipient.id")
    @Mapping(target = "senderId", source = "sender.id")
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "status", source = "status")
    NotificationResponseDTO toResponse(Notification entity);

    default NotificationResponseDTO toResponseDTO(Notification entity) {
        return toResponse(entity);
    }
}
