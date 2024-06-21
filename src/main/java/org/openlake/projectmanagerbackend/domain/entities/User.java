package org.openlake.projectmanagerbackend.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
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
public class User {
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
    @NotNull(message = "Password cannot be empty or null")
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToMany(mappedBy = "users",cascade = CascadeType.ALL)
    @JsonIgnoreProperties("users")
    private List<Project> projects = new ArrayList<>();
}