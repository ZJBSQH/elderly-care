package com.elderlycare.filter;

import com.elderlycare.common.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    /**
     * 拦截所有请求，对请求头中的 Authorization 字段进行解析，获取 token，并验证 token
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = getTokenFromRequest(request);

        String uri = request.getRequestURI();

        if (uri.startsWith("/ws/")) {
            // WebSocket 升级请求直接放行，交给 WebSocket 容器处理
            filterChain.doFilter(request, response);
            return;
        }

        // 1. 获取 token
        if (StringUtils.hasText(token)) {
            try {
                if (jwtUtil.validateToken(token)) {
                    Integer userId = jwtUtil.getUserIdFromToken(token);
                    String phone = jwtUtil.getPhoneFromToken(token);

                    User userDetails = new User(
                            phone,
                            "",
                            new ArrayList<>()
                    );

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    log.debug("用户认证成功，userId: {}, phone: {}", userId, phone);
                } else {
                    log.warn("Token 验证失败");
                }
            } catch (Exception e) {
                log.error("Token 解析失败：{}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中获取 token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}

