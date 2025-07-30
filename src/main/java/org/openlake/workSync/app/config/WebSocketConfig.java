package org.openlake.workSync.app.config;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.HandshakeHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.openlake.workSync.app.security.JwtTokenProvider;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.openlake.workSync.app.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.openlake.workSync.app.domain.entity.User;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/user");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new JwtHandshakeInterceptor(jwtTokenProvider, userService))
                .setHandshakeHandler(new JwtHandshakeHandler())
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new JwtChannelInterceptor(jwtTokenProvider, userService));
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        // No interceptors needed for outbound messages
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new UserArgumentResolver(userService));
    }

    public static class JwtHandshakeInterceptor implements HandshakeInterceptor {
        private final JwtTokenProvider jwtTokenProvider;
        private final UserService userService;
        
        public JwtHandshakeInterceptor(JwtTokenProvider jwtTokenProvider, UserService userService) {
            this.jwtTokenProvider = jwtTokenProvider;
            this.userService = userService;
        }
        
        @Override
        public boolean beforeHandshake(org.springframework.http.server.ServerHttpRequest request, @Nonnull org.springframework.http.server.ServerHttpResponse response, @Nonnull WebSocketHandler wsHandler, @Nonnull Map<String, Object> attributes) {
            log.debug("WebSocket handshake initiated for: {}", request.getURI());
            
            String token = null;
            String query = request.getURI().getQuery();
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 2 && pair[0].equals("access_token")) {
                        token = pair[1];
                        log.debug("Found token in query parameter");
                        break;
                    }
                }
            }
            if (token == null) {
                String authHeader = request.getHeaders().getFirst("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                    log.debug("Found token in Authorization header");
                }
            }
            
            if (token == null) {
                log.warn("No JWT token found in WebSocket handshake");
                return false;
            }
            
            log.debug("Validating JWT token");
            if (jwtTokenProvider.validateToken(token)) {
                try {
                    UUID userId = jwtTokenProvider.getUserIdFromJWT(token);
                    log.debug("Token validated, user ID: {}", userId);
                    
                    UserDetails userDetails = userService.loadUserById(userId);
                    log.debug("User loaded: {}", userDetails.getUsername());
                    
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    attributes.put("user", userDetails);
                    attributes.put("authentication", authentication);
                    
                    log.debug("WebSocket handshake successful for user: {}", userDetails.getUsername());
                    return true;
                } catch (Exception e) {
                    log.error("Error during WebSocket handshake authentication: {}", e.getMessage(), e);
                    return false;
                }
            } else {
                log.warn("Invalid JWT token in WebSocket handshake");
                return false;
            }
        }
        
        @Override
        public void afterHandshake(@Nonnull org.springframework.http.server.ServerHttpRequest request, @Nonnull org.springframework.http.server.ServerHttpResponse response, @Nonnull WebSocketHandler wsHandler, Exception exception) {
            if (exception != null) {
                log.error("WebSocket handshake failed: {}", exception.getMessage(), exception);
            } else {
                log.debug("WebSocket handshake completed successfully");
            }
        }
    }

    public static class JwtHandshakeHandler extends DefaultHandshakeHandler {
        @Override
        protected java.security.Principal determineUser(org.springframework.http.server.ServerHttpRequest request, org.springframework.web.socket.WebSocketHandler wsHandler, Map<String, Object> attributes) {
            UserDetails userDetails = (UserDetails) attributes.get("user");
            UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) attributes.get("authentication");
            if (userDetails != null && authentication != null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Setting authentication for user: {}", userDetails.getUsername());
                return authentication;
            }
            log.warn("No user details found in handshake attributes");
            return super.determineUser(request, wsHandler, attributes);
        }
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

    public static class UserArgumentResolver implements HandlerMethodArgumentResolver {
        private final UserService userService;

        public UserArgumentResolver(UserService userService) {
            this.userService = userService;
        }

        @Override
        public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
            return parameter.getParameterType().equals(User.class);
        }

        @Override
        public Object resolveArgument(org.springframework.core.MethodParameter parameter, org.springframework.messaging.Message<?> message) {
            try {
                var auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getPrincipal() instanceof User) {
                    return auth.getPrincipal();
                }
                
                // If not in SecurityContext, try to get from the message headers
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && accessor.getUser() != null) {
                    var principal = accessor.getUser();
                    if (principal instanceof UsernamePasswordAuthenticationToken) {
                        var userDetails = (UserDetails) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
                        if (userDetails instanceof User) {
                            return userDetails;
                        }
                    }
                }
                
                log.warn("Could not resolve User from message context");
                return null;
            } catch (Exception e) {
                log.error("Error resolving User argument: {}", e.getMessage());
                return null;
            }
        }
    }
}
