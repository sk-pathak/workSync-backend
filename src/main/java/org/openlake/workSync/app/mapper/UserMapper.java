package org.openlake.workSync.app.mapper;

import org.mapstruct.*;
import org.openlake.workSync.app.domain.entity.User;
import org.openlake.workSync.app.dto.UserRequestDTO;
import org.openlake.workSync.app.dto.UserResponseDTO;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "role", target = "role", qualifiedByName = "roleToString")
    UserResponseDTO toResponseDTO(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", expression = "java(org.openlake.workSync.app.domain.enumeration.Role.USER)")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ownedProjects", ignore = true)
    @Mapping(target = "projects", ignore = true)
    @Mapping(target = "starredProjects", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    @Mapping(target = "assignedTasks", ignore = true)
    User toEntity(UserRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDTO(UserRequestDTO dto, @MappingTarget User user);

    @Named("roleToString")
    static String roleToString(org.openlake.workSync.app.domain.enumeration.Role role) {
        return role != null ? role.name() : null;
    }
}
