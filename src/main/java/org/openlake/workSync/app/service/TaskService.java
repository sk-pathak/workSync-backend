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
import org.openlake.workSync.app.domain.exception.ResourceNotFoundException;
import org.openlake.workSync.app.domain.exception.ValidationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.openlake.workSync.app.dto.PagedResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.openlake.workSync.app.domain.enumeration.TaskStatus;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepo taskRepo;
    private final ProjectRepo projectRepo;
    private final UserRepo userRepo;
    private final TaskMapper taskMapper;
    private final NotificationService notificationService;

    public PagedResponse<TaskResponseDTO> listTasks(UUID projectId, Pageable pageable) {
        Page<Task> page = taskRepo.findByProjectIdWithAssignee(projectId, pageable);
        return new PagedResponse<>(page.map(taskMapper::toResponse));
    }

    public TaskResponseDTO createTask(UUID creatorId, UUID projectId, TaskRequestDTO request) {
        Project project = projectRepo.findById(projectId)
            .orElseThrow(() -> ResourceNotFoundException.projectNotFound(projectId));
        User creator = userRepo.findById(creatorId)
            .orElseThrow(() -> ResourceNotFoundException.userNotFound(creatorId));
        Task task = taskMapper.toEntity(request);
        task.setProject(project);
        task.setCreator(creator);
        
        if (request.getAssigneeId() != null) {
            User assignee = userRepo.findById(request.getAssigneeId())
                .orElseThrow(() -> ResourceNotFoundException.userNotFound(request.getAssigneeId()));
            task.setAssignedTo(assignee);
        }
        
        taskRepo.save(task);
        TaskResponseDTO response = taskMapper.toResponse(task);
        
        return response;
    }

    public TaskResponseDTO updateTask(UUID projectId, UUID taskId, TaskRequestDTO request) {
        Task task = taskRepo.findByIdAndProjectIdWithAssignee(taskId, projectId)
            .orElseThrow(() -> ResourceNotFoundException.taskNotFound(taskId));
        
        taskMapper.updateEntityFromDTO(request, task);
        
        if (request.getAssigneeId() != null) {
            User assignee = userRepo.findById(request.getAssigneeId())
                .orElseThrow(() -> ResourceNotFoundException.userNotFound(request.getAssigneeId()));
            task.setAssignedTo(assignee);
        } else if (request.getAssigneeId() == null) {
            task.setAssignedTo(null);
        }
        
        taskRepo.save(task);
        Task updatedTask = taskRepo.findByIdAndProjectIdWithAssignee(taskId, projectId)
            .orElseThrow(() -> ResourceNotFoundException.taskNotFound(taskId));
        TaskResponseDTO response = taskMapper.toResponse(updatedTask);
        return response;
    }

    public void deleteTask(UUID projectId, UUID taskId) {
        Task task = taskRepo.findByIdAndProjectIdWithAssignee(taskId, projectId)
            .orElseThrow(() -> ResourceNotFoundException.taskNotFound(taskId));
        taskRepo.delete(task);
    }

    public TaskResponseDTO assignTask(UUID projectId, UUID taskId, UUID userId) {
        Task task = taskRepo.findByIdAndProjectIdWithAssignee(taskId, projectId)
            .orElseThrow(() -> ResourceNotFoundException.taskNotFound(taskId));
        User user = userRepo.findById(userId)
            .orElseThrow(() -> ResourceNotFoundException.userNotFound(userId));
        task.setAssignedTo(user);
        taskRepo.save(task);
        Project project = task.getProject();
        User sender = task.getCreator();
        notificationService.notifyTaskAssigned(user, sender, project, task);
        return taskMapper.toResponse(task);
    }

    public TaskResponseDTO updateTaskStatus(UUID projectId, UUID taskId, String status) {
        Task task = taskRepo.findByIdAndProjectIdWithAssignee(taskId, projectId)
            .orElseThrow(() -> ResourceNotFoundException.taskNotFound(taskId));
        try {
            TaskStatus taskStatus = TaskStatus.valueOf(status.toUpperCase());
            task.setStatus(taskStatus);
            taskRepo.save(task);
            return taskMapper.toResponse(task);
        } catch (IllegalArgumentException e) {
            throw ValidationException.invalidTaskStatus(status);
        }
    }
}
