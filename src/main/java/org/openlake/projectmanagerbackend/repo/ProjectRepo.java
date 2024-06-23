package org.openlake.projectmanagerbackend.repo;

import org.openlake.projectmanagerbackend.domain.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepo extends JpaRepository<ProjectEntity, Long> {
}
