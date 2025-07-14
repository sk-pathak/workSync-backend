package org.openlake.workSync.app.service;

import lombok.RequiredArgsConstructor;
import org.openlake.workSync.app.dto.GeneralAnalyticsDTO;
import org.openlake.workSync.app.domain.enumeration.ProjectStatus;
import org.openlake.workSync.app.domain.enumeration.TaskStatus;
import org.openlake.workSync.app.domain.enumeration.Role;
import org.openlake.workSync.app.repo.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private final UserRepo userRepo;
    private final ProjectRepo projectRepo;
    private final TaskRepo taskRepo;
    private final ChatRepo chatRepo;
    private final MessageRepo messageRepo;
    private final NotificationRepo notificationRepo;

    public GeneralAnalyticsDTO getGeneralAnalytics() {
        long totalUsers = userRepo.count();
        long totalProjects = projectRepo.count();
        long totalTasks = taskRepo.count();
        long totalChats = chatRepo.count();
        long totalMessages = messageRepo.count();
        long totalNotifications = notificationRepo.count();

        Map<String, Long> projectStatusBreakdown = new HashMap<>();
        for (ProjectStatus status : ProjectStatus.values()) {
            projectStatusBreakdown.put(status.name(), projectRepo.countByStatus(status));
        }

        Map<String, Long> taskStatusBreakdown = new HashMap<>();
        for (TaskStatus status : TaskStatus.values()) {
            taskStatusBreakdown.put(status.name(), taskRepo.countByStatus(status));
        }

        Map<String, Long> userRoleBreakdown = new HashMap<>();
        for (Role role : Role.values()) {
            userRoleBreakdown.put(role.name(), userRepo.countByRole(role));
        }

        return GeneralAnalyticsDTO.builder()
                .totalUsers(totalUsers)
                .totalProjects(totalProjects)
                .totalTasks(totalTasks)
                .totalChats(totalChats)
                .totalMessages(totalMessages)
                .totalNotifications(totalNotifications)
                .projectStatusBreakdown(projectStatusBreakdown)
                .taskStatusBreakdown(taskStatusBreakdown)
                .userRoleBreakdown(userRoleBreakdown)
                .build();
    }
}