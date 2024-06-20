package org.openlake.projectmanagerbackend.repo;

import org.openlake.projectmanagerbackend.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepo extends JpaRepository<Project, Long> {
}
