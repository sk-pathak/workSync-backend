package org.openlake.workSync.app.domain.exception;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static ResourceNotFoundException projectNotFound(UUID projectId) {
        return new ResourceNotFoundException("Project not found with ID: " + projectId);
    }
    
    public static ResourceNotFoundException userNotFound(UUID userId) {
        return new ResourceNotFoundException("User not found with ID: " + userId);
    }
    
    public static ResourceNotFoundException taskNotFound(UUID taskId) {
        return new ResourceNotFoundException("Task not found with ID: " + taskId);
    }
    
    public static ResourceNotFoundException chatNotFound(UUID chatId) {
        return new ResourceNotFoundException("Chat not found with ID: " + chatId);
    }
    
    public static ResourceNotFoundException notificationNotFound(UUID notificationId) {
        return new ResourceNotFoundException("Notification not found with ID: " + notificationId);
    }
} 