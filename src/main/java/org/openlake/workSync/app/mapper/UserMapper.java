package org.openlake.workSync.app.mapper;

import org.mapstruct.*;
import org.openlake.workSync.app.domain.entity.User;
import org.openlake.workSync.app.dto.UserRequestDTO;
import org.openlake.workSync.app.dto.UserResponseDTO;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mappings({
            @Mapping(source = "id", target = "id"),
            @Mapping(source = "username", target = "username"),
            @Mapping(source = "email", target = "email"),
            @Mapping(source = "name", target = "name"),
            @Mapping(source = "bio", target = "bio"),
            @Mapping(source = "avatarUrl", target = "avatarUrl"),
            @Mapping(source = "role", target = "role", qualifiedByName = "roleToString"),
            @Mapping(source = "createdAt", target = "createdAt"),
            @Mapping(source = "updatedAt", target = "updatedAt")
    })
    UserResponseDTO toResponseDTO(User user);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "username", source = "username"),
            @Mapping(target = "email", source = "email"),
            @Mapping(target = "password", source = "password"),
            @Mapping(target = "name", source = "name"),
            @Mapping(target = "bio", source = "bio"),
            @Mapping(target = "avatarUrl", source = "avatarUrl"),
            @Mapping(target = "role", expression = "java(org.openlake.workSync.app.domain.enumeration.Role.USER)"),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "ownedProjects", ignore = true),
            @Mapping(target = "projects", ignore = true),
            @Mapping(target = "starredProjects", ignore = true),
            @Mapping(target = "notifications", ignore = true),
            @Mapping(target = "assignedTasks", ignore = true),
    })
    User toEntity(UserRequestDTO dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "role", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "ownedProjects", ignore = true),
            @Mapping(target = "projects", ignore = true),
            @Mapping(target = "starredProjects", ignore = true),
            @Mapping(target = "notifications", ignore = true),
            @Mapping(target = "assignedTasks", ignore = true),
    })
    void updateUserFromDTO(UserRequestDTO dto, @MappingTarget User user);

    @Named("roleToString")
    static String roleToString(org.openlake.workSync.app.domain.enumeration.Role role) {
        return role != null ? role.name() : null;
    }
}
