package org.openlake.workSync.app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponseDTO {
    private String token;
    private String role;
    private String expirationTime;
    private UserResponseDTO user;
}
