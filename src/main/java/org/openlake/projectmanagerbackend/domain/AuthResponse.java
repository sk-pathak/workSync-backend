package org.openlake.projectmanagerbackend.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.openlake.projectmanagerbackend.domain.dto.Project;
import org.openlake.projectmanagerbackend.domain.dto.User;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse extends Response {
    private String token;
    private String role;
    private String expirationTime;
    private User user;
}
