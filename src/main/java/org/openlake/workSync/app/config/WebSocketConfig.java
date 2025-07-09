package org.openlake.workSync.app.config;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.openlake.workSync.app.security.JwtTokenProvider;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.openlake.workSync.app.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.WebSocketHandler;
import java.util.Map;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new JwtHandshakeInterceptor(jwtTokenProvider, userService))
                .withSockJS();
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
            String token = null;
            String query = request.getURI().getQuery();
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 2 && pair[0].equals("access_token")) {
                        token = pair[1];
                        break;
                    }
                }
            }
            if (token == null) {
                String authHeader = request.getHeaders().getFirst("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                }
            }
            if (token != null && jwtTokenProvider.validateToken(token)) {
                UUID userId = jwtTokenProvider.getUserIdFromJWT(token);
                UserDetails userDetails = userService.loadUserById(userId);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                attributes.put("user", userDetails);
                return true;
            }
            return false;
        }
        @Override
        public void afterHandshake( @Nonnull org.springframework.http.server.ServerHttpRequest request, @Nonnull org.springframework.http.server.ServerHttpResponse response, @Nonnull WebSocketHandler wsHandler, Exception exception) {}
    }
}
