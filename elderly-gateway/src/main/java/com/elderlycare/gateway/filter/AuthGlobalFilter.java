package com.elderlycare.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * 网关全局认证过滤器
 * 解析 JWT，将 userId/phone/userType 通过请求头传递给下游服务
 */
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret:mySecretKeyForJWTTokenGenerationWhichIsVeryLong123456}")
    private String secret;

    /** 无需认证的路径 */
    private static final String[] PERMIT_ALL = {
            "/auth/login", "/auth/register", "/auth/sms", "/auth/password/reset", "/ws/",
            "/ai/",           // AI 问答及知识库（认证由 elderly-ai 服务自行控制）
            "/health-knowledge/" // 健康知识资讯（公开阅读）
    };

    /**
     * 全局过滤：校验 JWT Token，将用户信息注入请求头后放行
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 放行公开路径
        if (isPermitAll(path)) {
            return chain.filter(exchange);
        }

        String token = getTokenFromRequest(exchange.getRequest());
        if (StringUtils.hasText(token)) {
            try {
                Claims claims = parseToken(token);
                Integer userId = claims.get("userId", Integer.class);
                String phone = claims.getSubject();

                if (userId == null) {
                    return unauthorized(exchange, "Token 无效");
                }

                // 将用户信息通过请求头传递给下游服务
                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header("X-User-Id", String.valueOf(userId))
                        .header("X-User-Phone", phone != null ? phone : "")
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            } catch (Exception e) {
                log.error("Token 解析失败: {}", e.getMessage());
                return unauthorized(exchange, "Token 无效或已过期");
            }
        }

        return unauthorized(exchange, "缺少认证信息");
    }

    /**
     * 判断当前请求路径是否属于公开路径（无需认证）
     */
    private boolean isPermitAll(String path) {
        for (String p : PERMIT_ALL) {
            if (path.startsWith(p)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从请求头中提取 Bearer Token
     */
    private String getTokenFromRequest(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 解析 JWT Token，返回其中的 Claims 载荷
     */
    private Claims parseToken(String token) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 返回 401 未授权响应并终止请求
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    /**
     * 返回过滤器执行顺序，值越小优先级越高
     */
    @Override
    public int getOrder() {
        return -100;
    }
}
