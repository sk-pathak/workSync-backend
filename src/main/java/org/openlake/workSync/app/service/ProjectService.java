package org.openlake.workSync.app.service;

import lombok.RequiredArgsConstructor;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.openlake.workSync.app.dto.PagedResponse;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepo projectRepo;
    private final UserRepo userRepo;
    private final ProjectMapper projectMapper;
    private final UserMapper userMapper;
    private final TaskMapper taskMapper;
    private final ProjectStarRepo projectStarRepo;
    private final ProjectMemberRepo projectMemberRepo;
    private final NotificationService notificationService;

    public PagedResponse<ProjectResponseDTO> listProjects(Pageable pageable) {
        Page<Project> page = projectRepo.findAll(pageable);
        return new PagedResponse<>(page.map(projectMapper::toResponseDTO));
    }

    public ProjectResponseDTO getProjectById(UUID id) {
        return projectRepo.findById(id).map(projectMapper::toResponseDTO).orElseThrow(() -> new RuntimeException("Project not found"));
    }

    public ProjectResponseDTO createProject(UUID ownerId, ProjectRequestDTO request) {
        User owner = userRepo.findById(ownerId).orElseThrow(() -> new RuntimeException("User not found"));
        Project project = projectMapper.toEntity(request);
        project.setOwner(owner);
        projectRepo.save(project);
        return projectMapper.toResponseDTO(project);
    }

    public ProjectResponseDTO updateProject(UUID id, ProjectRequestDTO request) {
        Project project = projectRepo.findById(id).orElseThrow(() -> new RuntimeException("Project not found"));
        projectMapper.updateEntityFromDTO(request, project);
        projectRepo.save(project);
        return projectMapper.toResponseDTO(project);
    }

    public void deleteProject(UUID id) {
        projectRepo.deleteById(id);
    }

    public void starProject(UUID userId, UUID projectId) {
        ProjectStarId id = new ProjectStarId(projectId, userId);
        if (!projectStarRepo.existsById(id)) {
            Project project = projectRepo.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));
            User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
            ProjectStar star = ProjectStar.builder().id(id).project(project).user(user).build();
            projectStarRepo.save(star);
        }
    }

    public void unstarProject(UUID userId, UUID projectId) {
        ProjectStarId id = new ProjectStarId(projectId, userId);
        if (projectStarRepo.existsById(id)) {
            projectStarRepo.deleteById(id);
        }
    }

    public void requestJoinProject(UUID userId, UUID projectId) {
        ProjectMemberId id = new ProjectMemberId(projectId, userId);
        if (!projectMemberRepo.existsById(id)) {
            Project project = projectRepo.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));
            User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
            ProjectMember member = ProjectMember.builder().id(id).project(project).user(user).projectRole("PENDING").build();
            projectMemberRepo.save(member);
            // Notify project owner
            notificationService.notifyJoinRequest(project.getOwner(), user, project);
        }
    }

    public PagedResponse<?> listMembers(UUID projectId, Pageable pageable) {
        List<ProjectMember> projectMembers = projectMemberRepo.findByProjectId(projectId);
        List<User> members = projectMembers.stream()
                .filter(pm -> "MEMBER".equals(pm.getProjectRole()))
                .map(ProjectMember::getUser)
                .toList();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), members.size());
        List<User> pagedMembers = (start < end) ? members.subList(start, end) : List.of();
        Page<User> page = new org.springframework.data.domain.PageImpl<>(pagedMembers, pageable, members.size());
        return new PagedResponse<>(page.map(userMapper::toResponseDTO));
    }

    public void approveMember(UUID projectId, UUID userId) {
        ProjectMemberId id = new ProjectMemberId(projectId, userId);
        ProjectMember member = projectMemberRepo.findById(id).orElseThrow(() -> new RuntimeException("Join request not found"));
        member.setProjectRole("MEMBER");
        projectMemberRepo.save(member);
        // Notify user
        Project project = member.getProject();
        User owner = project.getOwner();
        notificationService.notifyJoinApproval(member.getUser(), owner, project);
    }

    public void removeMember(UUID projectId, UUID userId) {
        ProjectMemberId id = new ProjectMemberId(projectId, userId);
        if (projectMemberRepo.existsById(id)) {
            projectMemberRepo.deleteById(id);
        }
    }

    public PagedResponse<?> listTasks(UUID projectId, Pageable pageable) {
        Project project = projectRepo.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));
        List<Task> tasks = project.getTasks();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), tasks.size());
        Page<Task> page = new org.springframework.data.domain.PageImpl<>(tasks.subList(start, end), pageable, tasks.size());
        return new PagedResponse<>(page.map(taskMapper::toResponseDTO));
    }

    // RBAC helpers for @PreAuthorize
    public boolean isOwnerOrAdmin(UUID projectId, UserDetails principal) {
        Project project = projectRepo.findById(projectId).orElse(null);
        if (project == null) return false;
        User user = (User) principal;
        return project.getOwner().getId().equals(user.getId()) || user.getRole().name().equals("ADMIN");
    }

    public boolean isMemberOrOwnerOrAdmin(UUID projectId, UserDetails principal) {
        Project project = projectRepo.findById(projectId).orElse(null);
        if (project == null) return false;
        User user = (User) principal;
        return project.getOwner().getId().equals(user.getId()) ||
                project.getMembers().stream().anyMatch(m -> m.getId().equals(user.getId())) ||
                user.getRole().name().equals("ADMIN");
    }
}
