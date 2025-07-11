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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.openlake.workSync.app.domain.payload.TaskAssignmentPayload;

import java.util.UUID;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepo notificationRepo;
    private final NotificationMapper notificationMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PagedResponse<NotificationResponseDTO> listNotifications(UUID userId, Pageable pageable) {
        Page<Notification> page = notificationRepo.findByRecipientId(userId, pageable);
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

    public void notifyTaskAssigned(User recipient, User sender, Project project, Task task) {
        TaskAssignmentPayload payload = new TaskAssignmentPayload(task.getId(), task.getTitle());
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
}
