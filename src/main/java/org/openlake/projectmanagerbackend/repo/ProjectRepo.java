package org.openlake.projectmanagerbackend.repo;

import jakarta.annotation.Nonnull;
import org.openlake.projectmanagerbackend.domain.entity.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepo extends JpaRepository<ProjectEntity, Long> {
    @Nonnull
    Page<ProjectEntity> findAll( Pageable pageable);
    boolean existsByProjectName(String projectName);
}
