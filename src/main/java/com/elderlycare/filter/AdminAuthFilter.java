package com.elderlycare.filter;

import com.elderlycare.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 管理员权限验证过滤器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        
        // 只拦截 /admin/ 开头的请求
        if (!requestURI.startsWith("/admin/")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 获取 Token
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            log.warn("管理后台接口未提供 Token: {}", requestURI);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未提供认证令牌\"}");
            return;
        }
        
        token = token.substring(7);
        
        try {
            // 解析 Token
            Claims claims = jwtUtil.parseToken(token);
            Integer userType = (Integer) claims.get("userType");
            
            // 检查是否为管理员（userType=2）
            if (userType == null || userType != 2) {
                log.warn("用户无权限访问管理后台，userType: {}, URI: {}", userType, requestURI);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403,\"message\":\"无权限访问\"}");
                return;
            }
            
            // 将用户信息存入请求属性，供后续使用
            request.setAttribute("userId", claims.get("userId"));
            request.setAttribute("userType", userType);
            
            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            log.error("Token 解析失败：{}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Token 无效或已过期\"}");
        }
    }
}
