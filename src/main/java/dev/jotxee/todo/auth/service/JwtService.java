package dev.jotxee.todo.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long accessExpirationSeconds;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") long accessExpirationSeconds) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationSeconds = accessExpirationSeconds;
    }

    public String generateAccessToken(String email) {
        long nowMs = System.currentTimeMillis();
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date(nowMs))
                .expiration(new Date(nowMs + accessExpirationSeconds * 1000))
                .signWith(secretKey)
                .compact();
    }

    public Optional<String> extractValidEmail(String token) {
        try {
            Claims claims = parseClaims(token);
            if (claims.getExpiration().after(new Date())) {
                return Optional.ofNullable(claims.getSubject());
            }
        } catch (Exception _) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
