package org.openlake.workSync.app.repo;

import org.openlake.workSync.app.domain.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProjectRepo extends JpaRepository<Project, UUID> {
    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.owner WHERE p.owner.id = :ownerId")
    Page<Project> findByOwnerId(@Param("ownerId") UUID ownerId, Pageable pageable);

    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.owner JOIN p.members m WHERE m.id = :userId")
    Page<Project> findByMemberId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.owner JOIN p.starredByUsers s WHERE s.id = :userId")
    Page<Project> findByStarredUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.owner")
    Page<Project> findAllWithOwner(Pageable pageable);

    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.owner WHERE p.id = :id")
    java.util.Optional<Project> findByIdWithOwner(@Param("id") UUID id);
}
