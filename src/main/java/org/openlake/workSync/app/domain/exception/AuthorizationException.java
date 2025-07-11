package org.openlake.workSync.app.domain.exception;

public class AuthorizationException extends RuntimeException {
    
    public AuthorizationException(String message) {
        super(message);
    }
    
    public AuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static AuthorizationException insufficientPermissions() {
        return new AuthorizationException("You don't have sufficient permissions to perform this action");
    }
    
    public static AuthorizationException notProjectOwner() {
        return new AuthorizationException("Only the project owner can perform this action");
    }
    
    public static AuthorizationException notProjectMember() {
        return new AuthorizationException("You must be a project member to perform this action");
    }
    
    public static AuthorizationException notTaskAssignee() {
        return new AuthorizationException("Only the assigned user can update this task");
    }
} 