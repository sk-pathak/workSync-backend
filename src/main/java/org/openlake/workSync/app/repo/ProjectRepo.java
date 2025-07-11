package org.openlake.workSync.app.repo;

import org.openlake.workSync.app.domain.entity.Project;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;

@Repository
public interface ProjectRepo extends JpaRepository<Project, UUID> {
    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.owner LEFT JOIN FETCH p.chat WHERE p.owner.id = :ownerId")
    Page<Project> findByOwnerId(@Param("ownerId") UUID ownerId, Pageable pageable);

    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.owner LEFT JOIN FETCH p.chat JOIN p.members m WHERE m.id = :userId")
    Page<Project> findByMemberId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.owner LEFT JOIN FETCH p.chat JOIN p.starredByUsers s WHERE s.id = :userId")
    Page<Project> findByStarredUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.owner LEFT JOIN FETCH p.chat")
    Page<Project> findAllWithOwner(Pageable pageable);

    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.owner WHERE p.id = :id")
    Optional<Project> findByIdWithOwner(@Param("id") UUID id);

    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.owner LEFT JOIN FETCH p.chat WHERE p.id = :id")
    Optional<Project> findByIdWithOwnerAndChat(@Param("id") UUID id);

    @Cacheable(value = "projectOwner", key = "#id")
    @Query("SELECT p.owner.id FROM Project p WHERE p.id = :id")
    Optional<UUID> findOwnerIdById(@Param("id") UUID id);

    @Query("SELECT COUNT(pm) > 0 FROM ProjectMember pm WHERE pm.project.id = :projectId AND pm.user.id = :userId")
    boolean isUserMember(@Param("projectId") UUID projectId, @Param("userId") UUID userId);
}
