package org.openlake.workSync.app.repo;

import org.openlake.workSync.app.domain.entity.ProjectLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectLinkRepo extends JpaRepository<ProjectLinkEntity, Long> {
}
