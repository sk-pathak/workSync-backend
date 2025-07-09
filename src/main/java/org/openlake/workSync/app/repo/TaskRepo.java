package org.openlake.workSync.app.repo;

import org.openlake.workSync.app.domain.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepo extends JpaRepository<Task, UUID> {
    List<Task> findByProjectId(UUID projectId);
    Optional<Task> findByIdAndProjectId(UUID taskId, UUID projectId);
    Page<Task> findByProjectId(UUID projectId, Pageable pageable);
}
