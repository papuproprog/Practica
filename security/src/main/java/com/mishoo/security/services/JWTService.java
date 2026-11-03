package com.mishoo.security.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JWTService {

    @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String secret;

    @Value("${jwt.expiration-ms:86400000}")
    private long expirationMs;

    public String generateToken(String username, String role, String branch) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("branch", branch != null && !branch.isBlank() ? branch : "centro");
        return createToken(claims, username);
    }

    public String generateToken(String username, String role) {
        return generateToken(username, role, "centro");
    }

    private String createToken(Map<String, Object> claims, String username) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> validateToken(final String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token);
    }

    public String extractUsername(String token) {
        return validateToken(token).getBody().getSubject();
    }

    public String extractRole(String token) {
        Object role = validateToken(token).getBody().get("role");
        return role != null ? role.toString() : "ADMIN";
    }

    public String extractBranch(String token) {
        Object branch = validateToken(token).getBody().get("branch");
        return branch != null ? branch.toString() : "centro";
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
