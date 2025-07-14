package org.openlake.workSync.app.repo;

import org.openlake.workSync.app.domain.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepo extends JpaRepository<Task, UUID> {
    List<Task> findByProjectId(UUID projectId);
    Optional<Task> findByIdAndProjectId(UUID taskId, UUID projectId);
    Page<Task> findByProjectId(UUID projectId, Pageable pageable);
    
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.assignedTo WHERE t.project.id = :projectId")
    List<Task> findByProjectIdWithAssignee(@Param("projectId") UUID projectId);
    
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.assignedTo WHERE t.project.id = :projectId")
    Page<Task> findByProjectIdWithAssignee(@Param("projectId") UUID projectId, Pageable pageable);
    
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.assignedTo WHERE t.id = :taskId AND t.project.id = :projectId")
    Optional<Task> findByIdAndProjectIdWithAssignee(@Param("taskId") UUID taskId, @Param("projectId") UUID projectId);

    long countByStatus(org.openlake.workSync.app.domain.enumeration.TaskStatus status);
}
