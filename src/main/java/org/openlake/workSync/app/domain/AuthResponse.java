package org.openlake.workSync.app.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.openlake.workSync.app.domain.dto.User;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse extends Response {
    private String token;
    private String role;
    private String expirationTime;
    private User user;
}
