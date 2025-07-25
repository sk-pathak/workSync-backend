package org.openlake.workSync.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.openlake.workSync.app.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = getJwtFromRequest(request);
        if (StringUtils.hasText(jwt)) {
            try {
                if (tokenProvider.validateToken(jwt)) {
                    UUID userId = tokenProvider.getUserIdFromJWT(jwt);
                    UserDetails userDetails = userService.loadUserById(userId);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (org.openlake.workSync.app.domain.exception.TokenExpiredException e) {
                handleTokenExpired(response, e.getMessage());
                return;
            } catch (Exception e) {
                System.err.println("JWT authentication failed: " + e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
    
    private void handleTokenExpired(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "TOKEN_EXPIRED");
        errorResponse.put("message", message);
        errorResponse.put("status", "UNAUTHORIZED");
        errorResponse.put("timestamp", System.currentTimeMillis());
        
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
