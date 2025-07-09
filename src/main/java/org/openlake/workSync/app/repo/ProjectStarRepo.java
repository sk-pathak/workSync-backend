package org.openlake.workSync.app.repo;

import org.openlake.workSync.app.domain.entity.ProjectStar;
import org.openlake.workSync.app.domain.entity.ProjectStarId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectStarRepo extends JpaRepository<ProjectStar, ProjectStarId> {
    Optional<ProjectStar> findById(ProjectStarId id);
    void deleteById(ProjectStarId id);
    boolean existsById(ProjectStarId id);
}
