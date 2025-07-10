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
import org.openlake.workSync.app.domain.entity.Chat;
import org.openlake.workSync.app.repo.ChatRepo;
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
    private final ChatRepo chatRepo;

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
            ProjectMember member = ProjectMember.builder().id(id).project(project).user(user).build();
            projectMemberRepo.save(member);
            notificationService.notifyJoinRequest(project.getOwner(), user, project);
        }
    }

    public PagedResponse<?> listMembers(UUID projectId, Pageable pageable) {
        List<ProjectMember> projectMembers = projectMemberRepo.findByProjectId(projectId);
        List<User> members = projectMembers.stream()
                .map(ProjectMember::getUser)
                .toList();
        
        int totalSize = members.size();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), totalSize);
        
        List<User> pagedMembers;
        if (start < totalSize && start < end) {
            pagedMembers = members.subList(start, end);
        } else {
            pagedMembers = List.of();
        }
        
        Page<User> page = new org.springframework.data.domain.PageImpl<>(pagedMembers, pageable, totalSize);
        return new PagedResponse<>(page.map(userMapper::toResponseDTO));
    }

    public void approveMember(UUID projectId, UUID userId) {
        Project project = projectRepo.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        notificationService.notifyJoinApproval(user, project.getOwner(), project);
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
