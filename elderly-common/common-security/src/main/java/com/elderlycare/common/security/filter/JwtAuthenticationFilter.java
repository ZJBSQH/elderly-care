package com.elderlycare.common.security.filter;

import com.elderlycare.common.security.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JWT 认证过滤器
 * 解析请求头中的 JWT，将用户 ID 放入 Authentication.details，并将用户类型转换为角色权限。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Integer USER_TYPE_ELDER = 0;
    private static final Integer USER_TYPE_FAMILY = 1;
    private static final Integer USER_TYPE_ADMIN = 2;

    private final JwtUtil jwtUtil;

    /**
     * 公共端点，进入 Spring Security 前直接跳过 token 解析。
     */
    private static final String[] PUBLIC_PATHS = {
            "/auth/login", "/auth/register", "/auth/sms",
            "/auth/password/reset", "/auth/user/",
            "/ws/"
    };

    /**
     * 从请求中解析 JWT，验证通过后写入 Spring Security 上下文。
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();
        if (isPublicPath(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = getTokenFromRequest(request);
        if (StringUtils.hasText(token)) {
            try {
                if (jwtUtil.validateToken(token)) {
                    Integer userId = jwtUtil.getUserIdFromToken(token);
                    String phone = jwtUtil.getPhoneFromToken(token);
                    Integer userType = jwtUtil.getUserTypeFromToken(token);

                    User userDetails = new User(phone, "", buildAuthorities(userType));
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    // 核心逻辑：只把 userId 放入 details，兼容现有 SecurityUtil.getCurrentUserId()。
                    authentication.setDetails(userId);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("用户认证成功，userId: {}, phone: {}, userType: {}", userId, phone, userType);
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
     * 判断请求路径是否属于公共端点。
     */
    private boolean isPublicPath(String uri) {
        for (String path : PUBLIC_PATHS) {
            if (uri.startsWith(path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从 Authorization 请求头中提取 Bearer Token。
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 根据用户类型构建角色权限，供后台管理和业务权限判断使用。
     */
    private List<SimpleGrantedAuthority> buildAuthorities(Integer userType) {
        if (USER_TYPE_ADMIN.equals(userType)) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        if (USER_TYPE_FAMILY.equals(userType)) {
            return List.of(new SimpleGrantedAuthority("ROLE_FAMILY"));
        }
        if (USER_TYPE_ELDER.equals(userType)) {
            return List.of(new SimpleGrantedAuthority("ROLE_ELDER"));
        }
        return new ArrayList<>();
    }
}
