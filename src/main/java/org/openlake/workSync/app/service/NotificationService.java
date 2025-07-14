package org.openlake.workSync.app.service;

import lombok.RequiredArgsConstructor;
import org.openlake.workSync.app.domain.entity.Notification;
import org.openlake.workSync.app.domain.enumeration.NotificationStatus;
import org.openlake.workSync.app.dto.NotificationResponseDTO;
import org.openlake.workSync.app.mapper.NotificationMapper;
import org.openlake.workSync.app.repo.NotificationRepo;
import org.openlake.workSync.app.domain.exception.ResourceNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.openlake.workSync.app.dto.PagedResponse;
import org.springframework.stereotype.Service;
import org.openlake.workSync.app.domain.entity.User;
import org.openlake.workSync.app.domain.entity.Project;
import org.openlake.workSync.app.domain.entity.Task;
import org.openlake.workSync.app.domain.enumeration.NotificationType;
import org.openlake.workSync.app.domain.enumeration.TaskStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.openlake.workSync.app.domain.payload.TaskAssignmentPayload;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepo notificationRepo;
    private final NotificationMapper notificationMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PagedResponse<NotificationResponseDTO> listNotifications(UUID userId, Pageable pageable) {
        Page<Notification> page = notificationRepo.findByRecipientIdAndStatusNot(userId, NotificationStatus.DISMISSED, pageable);
        return new PagedResponse<>(page.map(notificationMapper::toResponse));
    }
    
    public PagedResponse<NotificationResponseDTO> listDismissedNotifications(UUID userId, Pageable pageable) {
        Page<Notification> page = notificationRepo.findByRecipientIdAndStatus(userId, NotificationStatus.DISMISSED, pageable);
        return new PagedResponse<>(page.map(notificationMapper::toResponse));
    }

    public void markAsRead(UUID userId, UUID notificationId) {
        Notification notification = notificationRepo.findByIdAndRecipientId(notificationId, userId)
            .orElseThrow(() -> ResourceNotFoundException.notificationNotFound(notificationId));
        notification.setStatus(NotificationStatus.READ);
        notificationRepo.save(notification);
    }

    public void dismiss(UUID userId, UUID notificationId) {
        Notification notification = notificationRepo.findByIdAndRecipientId(notificationId, userId)
            .orElseThrow(() -> ResourceNotFoundException.notificationNotFound(notificationId));
        notification.setStatus(NotificationStatus.DISMISSED);
        notificationRepo.save(notification);
    }

    public void markAllAsRead(UUID userId) {
        try {
            List<Notification> notifications = notificationRepo.findByRecipientIdAndStatusNot(userId, NotificationStatus.DISMISSED);
            if (notifications.isEmpty()) {
                return;
            }
            notifications.forEach(notification -> notification.setStatus(NotificationStatus.READ));
            notificationRepo.saveAll(notifications);
        } catch (Exception e) {
            System.err.println("Error in markAllAsRead for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public void dismissAll(UUID userId) {
        try {
            List<Notification> notifications = notificationRepo.findByRecipientIdAndStatusNot(userId, NotificationStatus.DISMISSED);
            if (notifications.isEmpty()) {
                return;
            }
            notifications.forEach(notification -> notification.setStatus(NotificationStatus.DISMISSED));
            notificationRepo.saveAll(notifications);
        } catch (Exception e) {
            System.err.println("Error in dismissAll for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public long getUnreadCount(UUID userId) {
        return notificationRepo.findByRecipientIdAndStatusNot(userId, NotificationStatus.DISMISSED)
            .stream()
            .filter(n -> n.getStatus() == NotificationStatus.PENDING)
            .count();
    }

    public void notifyJoinRequest(User recipient, User sender, Project project) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .sender(sender)
                .project(project)
                .type(NotificationType.JOIN_REQUEST)
                .status(NotificationStatus.PENDING)
                .payload(null)
                .build();
        notificationRepo.save(notification);
    }

    public void notifyJoinApproval(User recipient, User sender, Project project) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .sender(sender)
                .project(project)
                .type(NotificationType.JOIN_APPROVED)
                .status(NotificationStatus.READ)
                .payload(null)
                .build();
        notificationRepo.save(notification);
    }

    public void notifyJoinRejection(User recipient, User sender, Project project) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .sender(sender)
                .project(project)
                .type(NotificationType.JOIN_REJECTED)
                .status(NotificationStatus.READ)
                .payload(null)
                .build();
        notificationRepo.save(notification);
    }

    public void notifyMemberRemoved(User recipient, User sender, Project project) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .sender(sender)
                .project(project)
                .type(NotificationType.MEMBER_REMOVED)
                .status(NotificationStatus.READ)
                .payload(null)
                .build();
        notificationRepo.save(notification);
    }

    public void notifyTaskAssigned(User recipient, User sender, Project project, Task task) {
        TaskAssignmentPayload payload = TaskAssignmentPayload.builder()
                .taskId(task.getId())
                .taskTitle(task.getTitle())
                .taskDescription(task.getDescription())
                .dueDate(task.getDueDate())
                .priority(task.getPriority())
                .build();
        
        Notification notification = Notification.builder()
                .recipient(recipient)
                .sender(sender)
                .project(project)
                .type(NotificationType.TASK_ASSIGNED)
                .status(NotificationStatus.PENDING)
                .payload(payload)
                .build();
        notificationRepo.save(notification);
    }

    public void notifyTaskStatusChanged(User recipient, User sender, Project project, Task task, TaskStatus oldStatus, TaskStatus newStatus) {
        TaskAssignmentPayload payload = TaskAssignmentPayload.builder()
                .taskId(task.getId())
                .taskTitle(task.getTitle())
                .oldStatus(oldStatus.name())
                .newStatus(newStatus.name())
                .build();
        
        Notification notification = Notification.builder()
                .recipient(recipient)
                .sender(sender)
                .project(project)
                .type(NotificationType.TASK_STATUS_CHANGED)
                .status(NotificationStatus.PENDING)
                .payload(payload)
                .build();
        notificationRepo.save(notification);
    }

    public void notifyTaskDueSoon(User recipient, Task task) {
        TaskAssignmentPayload payload = TaskAssignmentPayload.builder()
                .taskId(task.getId())
                .taskTitle(task.getTitle())
                .dueDate(task.getDueDate())
                .build();
        
        Notification notification = Notification.builder()
                .recipient(recipient)
                .sender(null)
                .project(task.getProject())
                .type(NotificationType.TASK_DUE_SOON)
                .status(NotificationStatus.PENDING)
                .payload(payload)
                .build();
        notificationRepo.save(notification);
    }

    public void notifyTaskOverdue(User recipient, Task task) {
        TaskAssignmentPayload payload = TaskAssignmentPayload.builder()
                .taskId(task.getId())
                .taskTitle(task.getTitle())
                .dueDate(task.getDueDate())
                .build();
        
        Notification notification = Notification.builder()
                .recipient(recipient)
                .sender(null)
                .project(task.getProject())
                .type(NotificationType.TASK_OVERDUE)
                .status(NotificationStatus.PENDING)
                .payload(payload)
                .build();
        notificationRepo.save(notification);
    }

    public void notifyTaskCommented(User recipient, User sender, Project project, Task task, String comment, UUID commentId) {
        TaskAssignmentPayload payload = TaskAssignmentPayload.builder()
                .taskId(task.getId())
                .taskTitle(task.getTitle())
                .comment(comment)
                .commentId(commentId)
                .build();
        
        Notification notification = Notification.builder()
                .recipient(recipient)
                .sender(sender)
                .project(project)
                .type(NotificationType.TASK_COMMENTED)
                .status(NotificationStatus.PENDING)
                .payload(payload)
                .build();
        notificationRepo.save(notification);
    }

    public void notifyMentionedInComment(User recipient, User sender, Project project, Task task, String comment, UUID commentId) {
        TaskAssignmentPayload payload = TaskAssignmentPayload.builder()
                .taskId(task.getId())
                .taskTitle(task.getTitle())
                .comment(comment)
                .commentId(commentId)
                .build();
        
        Notification notification = Notification.builder()
                .recipient(recipient)
                .sender(sender)
                .project(project)
                .type(NotificationType.MENTIONED_IN_COMMENT)
                .status(NotificationStatus.PENDING)
                .payload(payload)
                .build();
        notificationRepo.save(notification);
    }

    // Project notifications
    public void notifyProjectUpdated(User recipient, User sender, Project project) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .sender(sender)
                .project(project)
                .type(NotificationType.PROJECT_UPDATED)
                .status(NotificationStatus.PENDING)
                .payload(null)
                .build();
        notificationRepo.save(notification);
    }

    public void notifyProjectStatusChanged(User recipient, User sender, Project project) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .sender(sender)
                .project(project)
                .type(NotificationType.PROJECT_STATUS_CHANGED)
                .status(NotificationStatus.PENDING)
                .payload(null)
                .build();
        notificationRepo.save(notification);
    }

    public boolean isTaskDueSoon(Task task) {
        if (task.getDueDate() == null) return false;
        long daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), task.getDueDate());
        return daysUntilDue >= 0 && daysUntilDue <= 3;
    }

    public boolean isTaskOverdue(Task task) {
        if (task.getDueDate() == null) return false;
        return LocalDate.now().isAfter(task.getDueDate());
    }
}
