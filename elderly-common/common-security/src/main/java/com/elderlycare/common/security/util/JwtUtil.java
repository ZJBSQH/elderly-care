package com.elderlycare.common.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret:mySecretKeyForJWTTokenGenerationWhichIsVeryLong123456}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    /**
     * 缓存的签名密钥，避免每次调用都重新创建
     */
    private volatile SecretKey cachedSecretKey;

    /** 获取签名密钥（双重检查锁，缓存复用） */
    private SecretKey getSigningKey() {
        if (cachedSecretKey == null) {
            synchronized (this) {
                if (cachedSecretKey == null) {
                    byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
                    cachedSecretKey = Keys.hmacShaKeyFor(keyBytes);
                }
            }
        }
        return cachedSecretKey;
    }

    /**
     * 生成 JWT token
     */
    public String generateToken(Integer userId, String phone, Integer userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("phone", phone);
        claims.put("userType", userType);
        return createToken(claims, phone);
    }

    /** 使用指定的声明和主题创建 JWT token */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从 token 中获取 userId
     */
    public Integer getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("userId", Integer.class);
    }

    /**
     * 从 token 中获取 phone
     */
    public String getPhoneFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.getSubject();
    }

    /**
     * 解析 token
     */
    public Claims parseToken(String token) {
        return getClaims(token);
    }

    /** 解析 token 并返回其声明信息 */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证 token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);
            Date expiration = claims.getExpiration();
            return !expiration.before(new Date());
        } catch (Exception e) {
            log.error("Token 验证失败：{}", e.getMessage());
            return false;
        }
    }

    /** 判断 token 是否已过期（过期返回 true） */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
