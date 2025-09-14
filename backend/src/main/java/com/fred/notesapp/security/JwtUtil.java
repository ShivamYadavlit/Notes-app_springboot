package com.fred.notesapp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    
    @Value("${jwt.secret:ThisIsASecretKeyForJWTUsingJJwtTokens}")
    private String secret;
    
    @Value("${jwt.expiration:86400000}")
    private Long expiration;
    
    // Create a consistent secret key for HS512
    private SecretKey getSigningKey() {
        // Print the secret for debugging
        System.out.println("Using JWT secret: " + secret);
        
        // Ensure we have exactly 64 bytes for HS512 by padding or truncating the secret
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        System.out.println("Secret bytes length: " + keyBytes.length);
        
        byte[] fixedKeyBytes = new byte[64];
        
        if (keyBytes.length >= 64) {
            // If we have 64 or more bytes, use the first 64
            System.arraycopy(keyBytes, 0, fixedKeyBytes, 0, 64);
        } else {
            // If we have fewer than 64 bytes, pad with zeros
            System.arraycopy(keyBytes, 0, fixedKeyBytes, 0, keyBytes.length);
            // Fill the rest with the first bytes of the secret repeated
            for (int i = keyBytes.length; i < 64; i++) {
                fixedKeyBytes[i] = keyBytes[i % keyBytes.length];
            }
        }
        
        // Print the base64 encoded key for debugging
        String encodedKey = Base64.getEncoder().encodeToString(fixedKeyBytes);
        System.out.println("Generated signing key (base64): " + encodedKey);
        
        return Keys.hmacShaKeyFor(fixedKeyBytes);
    }
    
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        try {
            System.out.println("Attempting to parse JWT token");
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (SignatureException e) {
            // Log the error for debugging
            System.err.println("JWT signature validation failed: " + e.getMessage());
            System.err.println("JWT token: " + token);
            throw e;
        } catch (Exception e) {
            System.err.println("Error parsing JWT: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    public String generateToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        System.out.println("Generating new JWT token for email: " + email);
        String token = createToken(claims, email);
        System.out.println("Generated token: " + token);
        return token;
    }
    
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    public Boolean validateToken(String token, String email) {
        try {
            final String extractedEmail = extractEmail(token);
            boolean isValid = (extractedEmail.equals(email) && !isTokenExpired(token));
            System.out.println("Token validation result for " + email + ": " + isValid);
            return isValid;
        } catch (Exception e) {
            System.err.println("Token validation failed: " + e.getMessage());
            return false;
        }
    }
}