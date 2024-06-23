package org.openlake.projectmanagerbackend.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.openlake.projectmanagerbackend.domain.enumeration.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {
    private Long userId;
    private String name;
    private String email;
    private String username;
    private String password;
    private Role role;
    private List<Project> projects;
}
