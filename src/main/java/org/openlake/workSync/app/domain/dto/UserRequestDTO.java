package org.openlake.workSync.app.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequestDTO {
    @NotBlank
    @Size(max = 50)
    private String username;

    @NotBlank @Email
    @Size(max = 100)
    private String email;

    @NotBlank @Size(min = 6, max = 100)
    private String password;

    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String bio;

    @Size(max = 512)
    private String avatarUrl;
}
