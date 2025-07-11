package org.openlake.workSync.app.domain.exception;

public class ValidationException extends RuntimeException {
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static ValidationException usernameAlreadyTaken(String username) {
        return new ValidationException("Username '" + username + "' is already taken");
    }
    
    public static ValidationException emailAlreadyRegistered(String email) {
        return new ValidationException("Email '" + email + "' is already registered");
    }
    
    public static ValidationException invalidTaskStatus(String status) {
        return new ValidationException("Invalid task status: " + status + ". Valid statuses are: TODO, IN_PROGRESS, DONE, BLOCKED");
    }
    
    public static ValidationException invalidProjectStatus(String status) {
        return new ValidationException("Invalid project status: " + status + ". Valid statuses are: PLANNED, ACTIVE, COMPLETED, ON_HOLD, CANCELLED");
    }
    
    public static ValidationException invalidGitHubUrl(String url) {
        return new ValidationException("Invalid GitHub repository URL: " + url);
    }
} 