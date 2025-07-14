package org.openlake.workSync.app.domain.exception;

public class TokenExpiredException extends RuntimeException {
    
    public TokenExpiredException(String message) {
        super(message);
    }
    
    public TokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static TokenExpiredException tokenExpired() {
        return new TokenExpiredException("JWT token has expired. Please login again.");
    }
    
    public static TokenExpiredException tokenExpired(Throwable cause) {
        return new TokenExpiredException("JWT token has expired. Please login again.", cause);
    }
}