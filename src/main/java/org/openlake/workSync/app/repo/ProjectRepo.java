package org.openlake.workSync.app.repo;

import jakarta.annotation.Nonnull;
import org.openlake.workSync.app.domain.entity.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepo extends JpaRepository<ProjectEntity, Long> {
    List<ProjectEntity> findAll();
    @Nonnull
    Page<ProjectEntity> findAll(Pageable pageable);

    boolean existsByProjectName(String projectName);

    @Query(value = "SELECT DISTINCT p.* FROM project p " +
            "JOIN project_tags t ON p.project_id = t.project_id " +
            "WHERE LOWER(p.project_name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(p.project_description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(t.tags) LIKE LOWER(CONCAT('%', :searchTerm, '%'))",
            nativeQuery = true)
    Page<ProjectEntity> searchKey(@Param("searchTerm") String searchTerm, Pageable pageable);
}
