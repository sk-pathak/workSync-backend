package org.openlake.workSync.app.domain.exception;

public class AuthenticationException extends RuntimeException {
    
    public AuthenticationException(String message) {
        super(message);
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException("Invalid username or password");
    }
    
    public static AuthenticationException userNotFound() {
        return new AuthenticationException("User not found");
    }
} 