package org.openlake.workSync.app.mapper;

import org.mapstruct.*;
import org.openlake.workSync.app.domain.entity.Task;
import org.openlake.workSync.app.dto.TaskRequestDTO;
import org.openlake.workSync.app.dto.TaskResponseDTO;
import org.openlake.workSync.app.domain.enumeration.TaskStatus;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "creatorId", source = "creator.id")
    @Mapping(target = "assigneeId", source = "assignedTo.id")
    @Mapping(target = "status", source = "status")
    TaskResponseDTO toResponse(Task entity);

    default TaskResponseDTO toResponseDTO(Task entity) {
        return toResponse(entity);
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", expression = "java(dto.getStatus() != null ? org.openlake.workSync.app.domain.enumeration.TaskStatus.valueOf(dto.getStatus()) : org.openlake.workSync.app.domain.enumeration.TaskStatus.TODO)")
    void updateEntityFromDTO(TaskRequestDTO dto, @MappingTarget Task entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", expression = "java(dto.getStatus() != null ? org.openlake.workSync.app.domain.enumeration.TaskStatus.valueOf(dto.getStatus()) : org.openlake.workSync.app.domain.enumeration.TaskStatus.TODO)")
    Task toEntity(TaskRequestDTO dto);
}
