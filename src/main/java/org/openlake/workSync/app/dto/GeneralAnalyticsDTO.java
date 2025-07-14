package org.openlake.workSync.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneralAnalyticsDTO {
    private long totalUsers;
    private long totalProjects;
    private long totalTasks;
    private long totalChats;
    private long totalMessages;
    private long totalNotifications;
    private Map<String, Long> projectStatusBreakdown;
    private Map<String, Long> taskStatusBreakdown;
    private Map<String, Long> userRoleBreakdown;
}