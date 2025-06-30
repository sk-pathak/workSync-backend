package org.openlake.workSync.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.openlake.workSync.app.domain.Response;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponseDTO extends Response {
    private String token;
    private String role;
    private String expirationTime;
    private UserResponseDTO user;
}
