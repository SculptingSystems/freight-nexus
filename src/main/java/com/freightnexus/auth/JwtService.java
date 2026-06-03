package com.freightnexus.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiry-hours:24}")
    private long expiryHours;

    public String generateToken(Long id, UserRole role) {
        long expiryMs = expiryHours * 3600 * 1000;
        return Jwts.builder()
                .subject(String.valueOf(id))
                .claim("role", role.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(signingKey())
                .compact();
    }

    public Optional<Claims> validate(String token) {
        try {
            return Optional.of(Jwts.parser().verifyWith(signingKey()).build()
                    .parseSignedClaims(token).getPayload());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public Long extractId(Claims claims) {
        return Long.parseLong(claims.getSubject());
    }

    public UserRole extractRole(Claims claims) {
        return UserRole.valueOf(claims.get("role", String.class));
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }
}
