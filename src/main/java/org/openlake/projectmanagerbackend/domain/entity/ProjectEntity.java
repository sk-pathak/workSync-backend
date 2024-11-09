package org.openlake.projectmanagerbackend.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "project")
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ProjectEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true, updatable = false)
    private Long projectId;

    @NotNull(message = "ProjectEntity name cannot be empty")
    private String projectName;

    @Column(columnDefinition = "TEXT")
    private String projectDescription;

    private String createdBy;

    private String sourceCodeLink;

    @ElementCollection(targetClass = String.class)
    @CollectionTable(name = "project_tags", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "tags", nullable = false)
    private List<String> tags=new ArrayList<>();

    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(
            name = "user_projects",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<UserEntity> userEntities = new ArrayList<>();
}
