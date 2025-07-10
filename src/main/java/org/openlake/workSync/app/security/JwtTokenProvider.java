package org.openlake.workSync.app.security;

import io.jsonwebtoken.*;
import org.openlake.workSync.app.domain.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secretKey}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}") // 1 day
    private int jwtExpirationMs;

    private SecretKey key;

    @PostConstruct
    public void init() {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(jwtSecret.getBytes(StandardCharsets.UTF_8));
            this.key = new SecretKeySpec(decodedKey, "HmacSHA256");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize JWT key", e);
        }
    }

    public String generateToken(Authentication authentication) {
        try {
            User userPrincipal = (User) authentication.getPrincipal();
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
            return Jwts.builder()
                    .subject(userPrincipal.getId().toString())
                    .claim("role", userPrincipal.getRole().name())
                    .issuedAt(now)
                    .expiration(expiryDate)
                    .signWith(key)
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }

    public UUID getUserIdFromJWT(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return UUID.fromString(claims.getSubject());
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract user ID from JWT token", e);
        }
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }
}
