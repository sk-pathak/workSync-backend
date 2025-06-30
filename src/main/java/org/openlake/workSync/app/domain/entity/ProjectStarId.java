package org.openlake.workSync.app.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ProjectStarId implements Serializable {
    @Column(name = "project_id", columnDefinition = "UUID")
    private UUID projectId;

    @Column(name = "user_id", columnDefinition = "UUID")
    private UUID userId;
}
