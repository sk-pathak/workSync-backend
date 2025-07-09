package org.openlake.workSync.app.repo;

import org.openlake.workSync.app.domain.entity.ProjectMember;
import org.openlake.workSync.app.domain.entity.ProjectMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectMemberRepo extends JpaRepository<ProjectMember, ProjectMemberId> {
    Optional<ProjectMember> findById(ProjectMemberId id);
    void deleteById(ProjectMemberId id);
    boolean existsById(ProjectMemberId id);
}
