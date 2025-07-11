package org.openlake.workSync.app.domain.exception;

public class ProjectMembershipException extends RuntimeException {
    
    public ProjectMembershipException(String message) {
        super(message);
    }
    
    public ProjectMembershipException(String message, Throwable cause) {
        super(message, cause);
    }
} 