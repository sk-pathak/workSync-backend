package org.openlake.workSync.app.service;

import lombok.RequiredArgsConstructor;
import org.openlake.workSync.app.domain.entity.Task;
import org.openlake.workSync.app.domain.entity.Project;
import org.openlake.workSync.app.domain.entity.User;
import org.openlake.workSync.app.dto.TaskRequestDTO;
import org.openlake.workSync.app.dto.TaskResponseDTO;
import org.openlake.workSync.app.mapper.TaskMapper;
import org.openlake.workSync.app.repo.TaskRepo;
import org.openlake.workSync.app.repo.ProjectRepo;
import org.openlake.workSync.app.repo.UserRepo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.openlake.workSync.app.dto.PagedResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepo taskRepo;
    private final ProjectRepo projectRepo;
    private final UserRepo userRepo;
    private final TaskMapper taskMapper;
    private final NotificationService notificationService;

    public PagedResponse<TaskResponseDTO> listTasks(UUID projectId, Pageable pageable) {
        Page<Task> page = taskRepo.findByProjectId(projectId, pageable);
        return new PagedResponse<>(page.map(taskMapper::toResponse));
    }

    public TaskResponseDTO createTask(UUID creatorId, UUID projectId, TaskRequestDTO request) {
        Project project = projectRepo.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));
        User creator = userRepo.findById(creatorId).orElseThrow(() -> new RuntimeException("User not found"));
        Task task = taskMapper.toEntity(request);
        task.setProject(project);
        task.setCreator(creator);
        taskRepo.save(task);
        return taskMapper.toResponse(task);
    }

    public TaskResponseDTO updateTask(UUID projectId, UUID taskId, TaskRequestDTO request) {
        Task task = taskRepo.findByIdAndProjectId(taskId, projectId).orElseThrow(() -> new RuntimeException("Task not found"));
        taskMapper.updateEntityFromDTO(request, task);
        taskRepo.save(task);
        return taskMapper.toResponse(task);
    }

    public void deleteTask(UUID projectId, UUID taskId) {
        Task task = taskRepo.findByIdAndProjectId(taskId, projectId).orElseThrow(() -> new RuntimeException("Task not found"));
        taskRepo.delete(task);
    }

    public TaskResponseDTO assignTask(UUID projectId, UUID taskId, UUID userId) {
        Task task = taskRepo.findByIdAndProjectId(taskId, projectId).orElseThrow(() -> new RuntimeException("Task not found"));
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        task.setAssignedTo(user);
        taskRepo.save(task);
        // Notify user
        Project project = task.getProject();
        User sender = task.getCreator();
        notificationService.notifyTaskAssigned(user, sender, project, task);
        return taskMapper.toResponse(task);
    }

    public TaskResponseDTO updateTaskStatus(UUID projectId, UUID taskId, String status) {
        Task task = taskRepo.findByIdAndProjectId(taskId, projectId).orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus(org.openlake.workSync.app.domain.enumeration.TaskStatus.valueOf(status.toUpperCase()));
        taskRepo.save(task);
        return taskMapper.toResponse(task);
    }
}
