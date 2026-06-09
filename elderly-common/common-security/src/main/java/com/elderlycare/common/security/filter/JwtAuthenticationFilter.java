package com.elderlycare.common.security.filter;

import com.elderlycare.common.security.util.JwtUtil;
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

/**
 * JWT 认证过滤器
 * 解析 JWT token，将 userId 存入 Authentication.details 供服务间使用
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    //公共端点
    private static final String[] PUBLIC_PATHS = {
            "/auth/login", "/auth/register", "/auth/sms",
            "/auth/password/reset", "/auth/user/",
            "/ws/"
    };

    /** 从请求中解析 JWT token，验证并设置 Spring Security 认证上下文 */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = getTokenFromRequest(request);
        String uri = request.getRequestURI();

        //加入公开端点跳过token检查
        if (isPublicPath(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (StringUtils.hasText(token)) {
            try {
                if (jwtUtil.validateToken(token)) {
                    Integer userId = jwtUtil.getUserIdFromToken(token);
                    String phone = jwtUtil.getPhoneFromToken(token);

                    User userDetails = new User(phone, "", new ArrayList<>());

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    // 关键改造: 将 userId 存入 details，使 SecurityUtil 无需查数据库
                    authentication.setDetails(userId);

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

    /** 判断请求路径是否为公共端点，无需认证即可访问 */
    private boolean isPublicPath(String uri) {

        //查找是否符合公共端点
        for (String path : PUBLIC_PATHS) {
            if (uri.startsWith(path)) {
                return true;
            }
        }
        //不符合返回
        return false;

    }

    /** 从请求头 Authorization 中提取 Bearer Token */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }



}
