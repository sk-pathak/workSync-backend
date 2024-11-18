package org.openlake.workSync.app.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.openlake.workSync.app.domain.enumeration.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class UserEntity implements UserDetails {
    @Id
    @Column(updatable = false, nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @NotNull(message = "Enter valid name")
    private String name;

    @Column(nullable = false, unique = true)
    @Email(message = "Please enter valid email")
    private String email;

    @NotNull(message = "Username cannot be empty or null")
    private String username;

    private String userProfile;

    @NotNull(message = "Password cannot be empty or null")
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String projectRole="";

    @ManyToMany(mappedBy = "userEntities",cascade = CascadeType.MERGE)
    @JsonIgnoreProperties("userEntities")
    private List<ProjectEntity> projectEntities = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_"+role.name()));
    }
}