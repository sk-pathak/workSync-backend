package org.openlake.workSync.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.openlake.workSync.app.domain.enumeration.Role;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {
    private Long userId;
    private String name;
    private String email;
    private String username;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Role role;
    private List<Project> projects;
}
