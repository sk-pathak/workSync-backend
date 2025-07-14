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
import org.openlake.workSync.app.repo.TaskRepo;
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
    private final TaskRepo taskRepo;
    private final NotificationService notificationService;
    private final ChatRepo chatRepo;

    private static final Logger debugLogger = LoggerFactory.getLogger(ProjectService.class);

    public PagedResponse<ProjectResponseDTO> listProjects(Pageable pageable) {
        Page<Project> page = projectRepo.findAllWithOwner(pageable);
        return new PagedResponse<>(page.map(this::enrichProjectWithMemberCount));
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
        return new PagedResponse<>(page.map(this::enrichProjectWithMemberCount));
    }

    public ProjectResponseDTO getProjectById(UUID id) {
        return projectRepo.findByIdWithOwnerAndChat(id)
            .map(this::enrichProjectWithMemberCount)
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
        return enrichProjectWithMemberCount(project);
    }

    public ProjectResponseDTO updateProject(UUID id, ProjectRequestDTO request) {
        log.debug("Updating project {} with request: {}", id, request);
        Project project = projectRepo.findByIdWithOwnerAndChat(id)
            .orElseThrow(() -> ResourceNotFoundException.projectNotFound(id));
        log.debug("Current project status before update: {}", project.getStatus());
        projectMapper.updateEntityFromDTO(request, project);
        log.debug("Project status after mapping: {}", project.getStatus());
        projectRepo.save(project);
        clearProjectCache(id);
        ProjectResponseDTO response = enrichProjectWithMemberCount(project);
        log.debug("Updated project response: {}", response);
        return response;
    }

    public void deleteProject(UUID id) {
        if (!projectRepo.existsById(id)) {
            throw ResourceNotFoundException.projectNotFound(id);
        }
        projectRepo.deleteById(id);
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
        clearProjectCache(projectId);
    }

    public PagedResponse<?> listMembers(UUID projectId, Pageable pageable) {
        Project project = projectRepo.findById(projectId)
            .orElseThrow(() -> ResourceNotFoundException.projectNotFound(projectId));
        
        List<ProjectMember> projectMembers = projectMemberRepo.findByProjectId(projectId);
        List<User> members = projectMembers.stream()
                .map(ProjectMember::getUser)
                .toList();
        
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
        clearProjectCache(projectId);
    }

    public void removeMember(UUID projectId, UUID userId) {
        Project project = projectRepo.findById(projectId)
            .orElseThrow(() -> ResourceNotFoundException.projectNotFound(projectId));
        
        if (project.getOwner().getId().equals(userId)) {
            throw new ProjectMembershipException("Cannot remove the project owner. Transfer ownership or delete the project.");
        }
        
        ProjectMemberId id = new ProjectMemberId(projectId, userId);
        if (!projectMemberRepo.existsById(id)) {
            throw new ProjectMembershipException("User is not a member of this project");
        }
        projectMemberRepo.deleteById(id);
        clearProjectCache(projectId);
    }

    public void leaveProject(UUID userId, UUID projectId) {
        Project project = projectRepo.findById(projectId)
            .orElseThrow(() -> ResourceNotFoundException.projectNotFound(projectId));
        
        if (project.getOwner().getId().equals(userId)) {
            throw new ProjectMembershipException("Project owner cannot leave the project. Transfer ownership or delete the project.");
        }
        
        ProjectMemberId id = new ProjectMemberId(projectId, userId);
        if (!projectMemberRepo.existsById(id)) {
            throw new ProjectMembershipException("You are not a member of this project");
        }
        
        removeMember(projectId, userId);
    }

    public PagedResponse<?> listTasks(UUID projectId, Pageable pageable) {
        Project project = projectRepo.findById(projectId)
            .orElseThrow(() -> ResourceNotFoundException.projectNotFound(projectId));
        List<Task> tasks = taskRepo.findByProjectIdWithAssignee(projectId);
        
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
        
        if (user.getRole().name().equals("ADMIN")) {
            log.debug("User {} is admin for project {}", principal.getUsername(), projectId);
            return true;
        }
        
        boolean isMember = isUserMember(projectId, user.getId());
        log.debug("Member authorization result for project {} and user {}: {}", projectId, principal.getUsername(), isMember);
        return isMember;
    }

    public boolean hasUserStarredProject(UUID userId, UUID projectId) {
        ProjectStarId id = new ProjectStarId(projectId, userId);
        return projectStarRepo.existsById(id);
    }

    public boolean isUserMember(UUID projectId, UUID userId) {
        if (isOwner(projectId, userId)) {
            return true;
        }
        return projectRepo.isUserMember(projectId, userId);
    }

    public boolean isOwner(UUID projectId, UUID userId) {
        UUID ownerId = projectRepo.findOwnerIdById(projectId).orElse(null);
        return ownerId != null && ownerId.equals(userId);
    }

    public Map<String, Object> getMembershipStatus(UUID projectId, UUID userId) {
        boolean isOwner = isOwner(projectId, userId);
        boolean isMember = isUserMember(projectId, userId);
        
        return Map.of(
            "isMember", isMember,
            "isOwner", isOwner,
            "canJoin", !isMember,
            "canLeave", isMember && !isOwner
        );
    }

    @CacheEvict(value = "projectOwner", key = "#projectId")
    private void clearProjectOwnerCache(UUID projectId) {
    }

    @CacheEvict(value = "projectAuthorization", allEntries = true)
    private void clearProjectAuthorizationCache(UUID projectId) {
    }

    private void clearProjectCache(UUID projectId) {
        clearProjectOwnerCache(projectId);
        clearProjectAuthorizationCache(projectId);
    }

    private ProjectResponseDTO enrichProjectWithMemberCount(Project project) {
        ProjectResponseDTO dto = projectMapper.toResponseDTO(project);
        int memberCount = calculateMemberCount(project.getId());
        dto.setMemberCount(memberCount);
        
        Map<String, Object> progressMetrics = calculateProjectProgress(project.getId());
        dto.setTotalTasks((Integer) progressMetrics.get("totalTasks"));
        dto.setCompletedTasks((Integer) progressMetrics.get("completedTasks"));
        dto.setProgressPercentage((Double) progressMetrics.get("progressPercentage"));
        dto.setTaskStatusBreakdown((Map<String, Integer>) progressMetrics.get("taskStatusBreakdown"));
        
        return dto;
    }

    private int calculateMemberCount(UUID projectId) {
        List<ProjectMember> projectMembers = projectMemberRepo.findByProjectId(projectId);
        return 1 + projectMembers.size();
    }

    private Map<String, Object> calculateProjectProgress(UUID projectId) {
        List<Task> tasks = taskRepo.findByProjectId(projectId);
        
        List<Task> activeTasks = tasks.stream()
            .filter(task -> task.getStatus() != org.openlake.workSync.app.domain.enumeration.TaskStatus.BLOCKED)
            .toList();
        
        int totalTasks = tasks.size();
        int activeTaskCount = activeTasks.size();
        int completedTasks = (int) activeTasks.stream()
            .filter(task -> task.getStatus() == org.openlake.workSync.app.domain.enumeration.TaskStatus.DONE)
            .count();
        
        double progressPercentage = activeTaskCount > 0 ? (double) completedTasks / activeTaskCount * 100 : 0.0;
        
        Map<String, Integer> taskStatusBreakdown = tasks.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                task -> task.getStatus().name(),
                java.util.stream.Collectors.collectingAndThen(
                    java.util.stream.Collectors.counting(),
                    Long::intValue
                )
            ));
        
        for (org.openlake.workSync.app.domain.enumeration.TaskStatus status : 
             org.openlake.workSync.app.domain.enumeration.TaskStatus.values()) {
            taskStatusBreakdown.putIfAbsent(status.name(), 0);
        }
        
        return Map.of(
            "totalTasks", totalTasks,
            "completedTasks", completedTasks,
            "progressPercentage", Math.round(progressPercentage * 100.0) / 100.0,
            "taskStatusBreakdown", taskStatusBreakdown
        );
    }
}
