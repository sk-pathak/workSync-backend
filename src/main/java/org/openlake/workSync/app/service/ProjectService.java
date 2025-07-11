package org.openlake.workSync.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openlake.workSync.app.domain.entity.Project;
import org.openlake.workSync.app.domain.entity.User;
import org.openlake.workSync.app.dto.ProjectRequestDTO;
import org.openlake.workSync.app.dto.ProjectResponseDTO;
import org.openlake.workSync.app.mapper.ProjectMapper;
import org.openlake.workSync.app.mapper.UserMapper;
import org.openlake.workSync.app.mapper.TaskMapper;
import org.openlake.workSync.app.domain.entity.Task;
import org.openlake.workSync.app.repo.ProjectRepo;
import org.openlake.workSync.app.repo.UserRepo;
import org.openlake.workSync.app.domain.entity.ProjectStar;
import org.openlake.workSync.app.domain.entity.ProjectStarId;
import org.openlake.workSync.app.domain.entity.ProjectMember;
import org.openlake.workSync.app.domain.entity.ProjectMemberId;
import org.openlake.workSync.app.repo.ProjectStarRepo;
import org.openlake.workSync.app.repo.ProjectMemberRepo;
import org.openlake.workSync.app.domain.entity.Chat;
import org.openlake.workSync.app.repo.ChatRepo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.openlake.workSync.app.dto.PagedResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.Map;
import org.openlake.workSync.app.domain.exception.ProjectMembershipException;
import org.openlake.workSync.app.domain.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import org.openlake.workSync.app.dto.ProjectFilterDTO;
import org.openlake.workSync.app.domain.enumeration.ProjectStatus;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProjectService {
    private final ProjectRepo projectRepo;
    private final UserRepo userRepo;
    private final ProjectMapper projectMapper;
    private final UserMapper userMapper;
    private final TaskMapper taskMapper;
    private final ProjectStarRepo projectStarRepo;
    private final ProjectMemberRepo projectMemberRepo;
    private final NotificationService notificationService;
    private final ChatRepo chatRepo;

    private static final Logger debugLogger = LoggerFactory.getLogger(ProjectService.class);

    public PagedResponse<ProjectResponseDTO> listProjects(Pageable pageable) {
        Page<Project> page = projectRepo.findAllWithOwner(pageable);
        return new PagedResponse<>(page.map(projectMapper::toResponseDTO));
    }

    public PagedResponse<ProjectResponseDTO> listProjectsWithFilters(ProjectFilterDTO filter, UUID userId, Pageable pageable) {
        Page<Project> page = projectRepo.findWithFilters(
            filter.getStatus(),
            filter.getOwnedByMe(),
            filter.getMemberOf(),
            filter.getStarred(),
            userId,
            pageable
        );
        return new PagedResponse<>(page.map(projectMapper::toResponseDTO));
    }

    public ProjectResponseDTO getProjectById(UUID id) {
        return projectRepo.findByIdWithOwnerAndChat(id)
            .map(projectMapper::toResponseDTO)
            .orElseThrow(() -> ResourceNotFoundException.projectNotFound(id));
    }

    public ProjectResponseDTO createProject(UUID ownerId, ProjectRequestDTO request) {
        User owner = userRepo.findById(ownerId)
            .orElseThrow(() -> ResourceNotFoundException.userNotFound(ownerId));
        Project project = projectMapper.toEntity(request);
        project.setOwner(owner);
        Chat chat = Chat.builder()
            .project(project)
            .name(project.getName() + " Chat")
            .build();
        project.setChat(chat);
        projectRepo.save(project);
        chatRepo.save(chat);
        return projectMapper.toResponseDTO(project);
    }

    public ProjectResponseDTO updateProject(UUID id, ProjectRequestDTO request) {
        log.debug("Updating project {} with request: {}", id, request);
        Project project = projectRepo.findByIdWithOwnerAndChat(id)
            .orElseThrow(() -> ResourceNotFoundException.projectNotFound(id));
        log.debug("Current project status before update: {}", project.getStatus());
        projectMapper.updateEntityFromDTO(request, project);
        log.debug("Project status after mapping: {}", project.getStatus());
        projectRepo.save(project);
        // Clear cache for this project
        clearProjectCache(id);
        ProjectResponseDTO response = projectMapper.toResponseDTO(project);
        log.debug("Updated project response: {}", response);
        return response;
    }

    public void deleteProject(UUID id) {
        if (!projectRepo.existsById(id)) {
            throw ResourceNotFoundException.projectNotFound(id);
        }
        projectRepo.deleteById(id);
        // Clear cache for this project
        clearProjectCache(id);
    }

    public void starProject(UUID userId, UUID projectId) {
        ProjectStarId id = new ProjectStarId(projectId, userId);
        if (projectStarRepo.existsById(id)) {
            throw new ProjectMembershipException("You have already starred this project");
        }
        Project project = projectRepo.findById(projectId)
            .orElseThrow(() -> ResourceNotFoundException.projectNotFound(projectId));
        User user = userRepo.findById(userId)
            .orElseThrow(() -> ResourceNotFoundException.userNotFound(userId));
        ProjectStar star = ProjectStar.builder().id(id).project(project).user(user).build();
        projectStarRepo.save(star);
    }

    public void unstarProject(UUID userId, UUID projectId) {
        ProjectStarId id = new ProjectStarId(projectId, userId);
        if (!projectStarRepo.existsById(id)) {
            throw new ProjectMembershipException("You have not starred this project");
        }
        projectStarRepo.deleteById(id);
    }

    public void requestJoinProject(UUID userId, UUID projectId) {
        ProjectMemberId id = new ProjectMemberId(projectId, userId);
        boolean exists = projectMemberRepo.existsById(id);
        debugLogger.debug("Checking membership for projectId={}, userId={}: exists={}", projectId, userId, exists);
        
        // Check if user is already a member (this now includes owner check)
        if (isUserMember(projectId, userId)) {
            throw new ProjectMembershipException("You are already a member of this project");
        }
        
        Project project = projectRepo.findById(projectId)
            .orElseThrow(() -> ResourceNotFoundException.projectNotFound(projectId));
        User user = userRepo.findById(userId)
            .orElseThrow(() -> ResourceNotFoundException.userNotFound(userId));
        ProjectMember member = ProjectMember.builder().id(id).project(project).user(user).build();
        projectMemberRepo.save(member);
        notificationService.notifyJoinRequest(project.getOwner(), user, project);
        // Clear cache for this project
        clearProjectCache(projectId);
    }

    public PagedResponse<?> listMembers(UUID projectId, Pageable pageable) {
        Project project = projectRepo.findById(projectId)
            .orElseThrow(() -> ResourceNotFoundException.projectNotFound(projectId));
        
        // Get project members from the project_members table
        List<ProjectMember> projectMembers = projectMemberRepo.findByProjectId(projectId);
        List<User> members = projectMembers.stream()
                .map(ProjectMember::getUser)
                .toList();
        
        // Add the project owner to the members list
        List<User> allMembers = new ArrayList<>(members);
        allMembers.add(project.getOwner());
        
        int totalSize = allMembers.size();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), totalSize);
        
        List<User> pagedMembers;
        if (start < totalSize && start < end) {
            pagedMembers = allMembers.subList(start, end);
        } else {
            pagedMembers = List.of();
        }
        
        Page<User> page = new org.springframework.data.domain.PageImpl<>(pagedMembers, pageable, totalSize);
        return new PagedResponse<>(page.map(userMapper::toResponseDTO));
    }

    public void approveMember(UUID projectId, UUID userId) {
        Project project = projectRepo.findById(projectId)
            .orElseThrow(() -> ResourceNotFoundException.projectNotFound(projectId));
        User user = userRepo.findById(userId)
            .orElseThrow(() -> ResourceNotFoundException.userNotFound(userId));
        notificationService.notifyJoinApproval(user, project.getOwner(), project);
        // Clear cache for this project
        clearProjectCache(projectId);
    }

    public void removeMember(UUID projectId, UUID userId) {
        Project project = projectRepo.findById(projectId)
            .orElseThrow(() -> ResourceNotFoundException.projectNotFound(projectId));
        
        // Prevent removing the project owner
        if (project.getOwner().getId().equals(userId)) {
            throw new ProjectMembershipException("Cannot remove the project owner. Transfer ownership or delete the project.");
        }
        
        ProjectMemberId id = new ProjectMemberId(projectId, userId);
        if (!projectMemberRepo.existsById(id)) {
            throw new ProjectMembershipException("User is not a member of this project");
        }
        projectMemberRepo.deleteById(id);
        // Clear cache for this project
        clearProjectCache(projectId);
    }

    public void leaveProject(UUID userId, UUID projectId) {
        Project project = projectRepo.findById(projectId)
            .orElseThrow(() -> ResourceNotFoundException.projectNotFound(projectId));
        
        // Check if user is the owner
        if (project.getOwner().getId().equals(userId)) {
            throw new ProjectMembershipException("Project owner cannot leave the project. Transfer ownership or delete the project.");
        }
        
        // Check if user is a member
        ProjectMemberId id = new ProjectMemberId(projectId, userId);
        if (!projectMemberRepo.existsById(id)) {
            throw new ProjectMembershipException("You are not a member of this project");
        }
        
        removeMember(projectId, userId);
    }

    public PagedResponse<?> listTasks(UUID projectId, Pageable pageable) {
        Project project = projectRepo.findById(projectId)
            .orElseThrow(() -> ResourceNotFoundException.projectNotFound(projectId));
        List<Task> tasks = project.getTasks();
        
        int totalSize = tasks.size();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), totalSize);
        
        List<Task> pagedTasks;
        if (start < totalSize && start < end) {
            pagedTasks = tasks.subList(start, end);
        } else {
            pagedTasks = List.of();
        }
        
        Page<Task> page = new org.springframework.data.domain.PageImpl<>(pagedTasks, pageable, totalSize);
        return new PagedResponse<>(page.map(taskMapper::toResponseDTO));
    }

    @Cacheable(value = "projectAuthorization", key = "#projectId + '_' + #principal.username")
    public boolean isOwnerOrAdmin(UUID projectId, UserDetails principal) {
        log.debug("Checking authorization for project {} and user {}", projectId, principal.getUsername());
        UUID ownerId = projectRepo.findOwnerIdById(projectId).orElse(null);
        if (ownerId == null) return false;
        User user = (User) principal;
        boolean isAuthorized = ownerId.equals(user.getId()) || user.getRole().name().equals("ADMIN");
        log.debug("Authorization result for project {} and user {}: {}", projectId, principal.getUsername(), isAuthorized);
        return isAuthorized;
    }

    @Cacheable(value = "projectAuthorization", key = "#projectId + '_member_' + #principal.username")
    public boolean isMemberOrOwnerOrAdmin(UUID projectId, UserDetails principal) {
        log.debug("Checking member authorization for project {} and user {}", projectId, principal.getUsername());
        User user = (User) principal;
        
        // Check if user is admin
        if (user.getRole().name().equals("ADMIN")) {
            log.debug("User {} is admin for project {}", principal.getUsername(), projectId);
            return true;
        }
        
        // Check if user is a member (this now includes owner check)
        boolean isMember = isUserMember(projectId, user.getId());
        log.debug("Member authorization result for project {} and user {}: {}", projectId, principal.getUsername(), isMember);
        return isMember;
    }

    /**
     * Check if a user has starred a project
     */
    public boolean hasUserStarredProject(UUID userId, UUID projectId) {
        ProjectStarId id = new ProjectStarId(projectId, userId);
        return projectStarRepo.existsById(id);
    }

    /**
     * Check if a user is a member of the project
     */
    public boolean isUserMember(UUID projectId, UUID userId) {
        // Check if user is the owner first
        if (isOwner(projectId, userId)) {
            return true;
        }
        // Then check if user is in the project_members table
        return projectRepo.isUserMember(projectId, userId);
    }

    /**
     * Check if a user is the owner of the project
     */
    public boolean isOwner(UUID projectId, UUID userId) {
        UUID ownerId = projectRepo.findOwnerIdById(projectId).orElse(null);
        return ownerId != null && ownerId.equals(userId);
    }

    /**
     * Get comprehensive membership status for a user in a project
     */
    public Map<String, Object> getMembershipStatus(UUID projectId, UUID userId) {
        boolean isOwner = isOwner(projectId, userId);
        boolean isMember = isUserMember(projectId, userId); // This now includes owner check
        
        return Map.of(
            "isMember", isMember,
            "isOwner", isOwner,
            "canJoin", !isMember, // Owner can't join since they're already a member
            "canLeave", isMember && !isOwner // Only non-owners can leave
        );
    }

    @CacheEvict(value = "projectOwner", key = "#projectId")
    private void clearProjectOwnerCache(UUID projectId) {
        // This method is used to clear project owner cache entries
    }

    @CacheEvict(value = "projectAuthorization", allEntries = true)
    private void clearProjectAuthorizationCache(UUID projectId) {
        // This method is used to clear project authorization cache entries
    }

    private void clearProjectCache(UUID projectId) {
        clearProjectOwnerCache(projectId);
        clearProjectAuthorizationCache(projectId);
    }
}
