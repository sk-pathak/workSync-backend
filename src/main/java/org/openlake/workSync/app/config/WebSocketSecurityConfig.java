package org.openlake.workSync.app.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.openlake.workSync.app.security.JwtTokenProvider;
import org.openlake.workSync.app.service.UserService;

import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
@Slf4j
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new JwtChannelInterceptor(jwtTokenProvider, userService));
    }

    public static class JwtChannelInterceptor implements ChannelInterceptor {
        private final JwtTokenProvider jwtTokenProvider;
        private final UserService userService;

        public JwtChannelInterceptor(JwtTokenProvider jwtTokenProvider, UserService userService) {
            this.jwtTokenProvider = jwtTokenProvider;
            this.userService = userService;
        }

        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
            
            if (accessor != null) {
                log.debug("Processing STOMP message: {} for session: {}", accessor.getCommand(), accessor.getSessionId());
                
                // Always try to authenticate for each message to ensure SecurityContext is set
                UsernamePasswordAuthenticationToken authentication = null;
                
                // First try to get from existing user
                if (accessor.getUser() != null) {
                    log.debug("User already authenticated: {}", accessor.getUser().getName());
                    authentication = (UsernamePasswordAuthenticationToken) accessor.getUser();
                } else {
                    // Try to authenticate from Authorization header
                    String auth = accessor.getFirstNativeHeader("Authorization");
                    if (auth != null && auth.startsWith("Bearer ")) {
                        String token = auth.substring(7);
                        if (jwtTokenProvider.validateToken(token)) {
                            try {
                                UUID userId = jwtTokenProvider.getUserIdFromJWT(token);
                                UserDetails userDetails = userService.loadUserById(userId);
                                authentication = new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                                accessor.setUser(authentication);
                                log.debug("User authenticated in {}: {}", accessor.getCommand(), userDetails.getUsername());
                            } catch (Exception e) {
                                log.error("Error authenticating user in {}: {}", accessor.getCommand(), e.getMessage());
                            }
                        }
                    }
                }
                
                // Set authentication in SecurityContext for this message processing
                if (authentication != null) {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("SecurityContext set for user: {}", authentication.getName());
                } else {
                    log.warn("No authentication found for message: {}", accessor.getCommand());
                }
            }
            
            return message;
        }
    }
}
