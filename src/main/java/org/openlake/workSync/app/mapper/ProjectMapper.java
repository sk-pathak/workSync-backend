package org.openlake.workSync.app.mapper;

import org.mapstruct.*;
import org.openlake.workSync.app.domain.entity.Project;
import org.openlake.workSync.app.dto.ProjectRequestDTO;
import org.openlake.workSync.app.dto.ProjectResponseDTO;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true) // default handled by entity
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "starredByUsers", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    @Mapping(target = "chat", ignore = true)
    Project toEntity(ProjectRequestDTO dto);

    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "status", source = "status")
    ProjectResponseDTO toResponse(Project entity);

    default ProjectResponseDTO toResponseDTO(Project entity) {
        return toResponse(entity);
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "starredByUsers", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    @Mapping(target = "chat", ignore = true)
    void updateEntityFromDTO(ProjectRequestDTO dto, @MappingTarget Project entity);
}
