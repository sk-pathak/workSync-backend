package org.openlake.workSync.app.repo;

import org.openlake.workSync.app.domain.entity.Project;
import org.openlake.workSync.app.domain.enumeration.ProjectStatus;
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

    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.owner LEFT JOIN FETCH p.chat WHERE p.status = :status")
    Page<Project> findByStatus(@Param("status") ProjectStatus status, Pageable pageable);

    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.owner LEFT JOIN FETCH p.chat WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Project> findByNameContaining(@Param("search") String search, Pageable pageable);

    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.owner LEFT JOIN FETCH p.chat WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) AND p.status = :status")
    Page<Project> findByNameContainingAndStatus(@Param("search") String search, @Param("status") ProjectStatus status, Pageable pageable);

    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.owner LEFT JOIN FETCH p.chat WHERE p.status = :status AND p.owner.id = :userId")
    Page<Project> findByStatusAndOwnerId(@Param("status") ProjectStatus status, @Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.owner LEFT JOIN FETCH p.chat WHERE p.status = :status AND EXISTS (SELECT pm FROM ProjectMember pm WHERE pm.project = p AND pm.user.id = :userId)")
    Page<Project> findByStatusAndMemberId(@Param("status") ProjectStatus status, @Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.owner LEFT JOIN FETCH p.chat WHERE p.status = :status AND EXISTS (SELECT ps FROM ProjectStar ps WHERE ps.project = p AND ps.user.id = :userId)")
    Page<Project> findByStatusAndStarredUserId(@Param("status") ProjectStatus status, @Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.owner LEFT JOIN FETCH p.chat WHERE " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:ownedByMe IS NULL OR (:ownedByMe = true AND p.owner.id = :userId) OR (:ownedByMe = false AND p.owner.id != :userId)) AND " +
           "(:memberOf IS NULL OR (:memberOf = true AND (p.owner.id = :userId OR EXISTS (SELECT pm FROM ProjectMember pm WHERE pm.project = p AND pm.user.id = :userId))) OR (:memberOf = false AND p.owner.id != :userId AND NOT EXISTS (SELECT pm FROM ProjectMember pm WHERE pm.project = p AND pm.user.id = :userId))) AND " +
           "(:starred IS NULL OR (:starred = true AND EXISTS (SELECT ps FROM ProjectStar ps WHERE ps.project = p AND ps.user.id = :userId)) OR (:starred = false AND NOT EXISTS (SELECT ps FROM ProjectStar ps WHERE ps.project = p AND ps.user.id = :userId)))")
    Page<Project> findWithFilters(@Param("status") ProjectStatus status, 
                                 @Param("ownedByMe") Boolean ownedByMe, 
                                 @Param("memberOf") Boolean memberOf, 
                                 @Param("starred") Boolean starred, 
                                 @Param("userId") UUID userId, 
                                 Pageable pageable);

    long countByStatus(ProjectStatus status);
}
