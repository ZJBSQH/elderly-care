package com.elderlycare.common.util;

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

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret:mySecretKeyForJWTTokenGenerationWhichIsVeryLong123456}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    //获取签名密钥
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    //生成token
    public String generateToken(Integer userId, String phone, Integer userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("phone", phone);
        claims.put("userType", userType);
        return createToken(claims, phone);
    }

    //创建token
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

    //获取用户ID
    public Integer getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.get("userId", Integer.class);
    }

    //获取手机号
    public String getPhoneFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.getSubject();
    }

    //获取 Claims
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    // 解析 Token（公开方法，供过滤器使用）
    public Claims parseToken(String token) {
        return getClaims(token);
    }

    //验证token
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

    //判断token是否过期
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}

