package com.diary.api.domain.user.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    public static String generateToken(String email, Long userId, String secretKey, long expirationTime) {
        String token = Jwts.builder()
                .setSubject(email)
                .claim("userId", userId) // 사용자 ID 클레임 추가
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
        logger.info("Generated JWT token for email: {}, userId: {}", email, userId);
        return token;
    }

    public static Long parseUserIdFromToken(String token, String secretKey) {
        Claims claims = extractClaims(token, secretKey);
        Long userId = claims.get("userId", Long.class);
        logger.info("Extracted userId: {} from token", userId);
        return userId;
    }

    public static Claims extractClaims(String token, String secretKey) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            logger.info("JWT token verified successfully. Claims: {}", claims);
            return claims;
        } catch (Exception e) {
            logger.error("Failed to verify JWT token: {}", e.getMessage());
            throw e;
        }
    }

    public static boolean isExpired(String token, String secretKey) {
        boolean expired = extractClaims(token, secretKey).getExpiration().before(new Date());
        logger.info("Token expired: {}", expired);
        return expired;
    }

    public static String getEmail(String token, String secretKey) {
        String email = extractClaims(token, secretKey).getSubject();
        logger.info("Extracted email: {} from token", email);
        return email;
    }
}
