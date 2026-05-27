package com.elderlycare.common.security.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Spring Security 工具类
 * 从 SecurityContext 直接读取 userId（由 JwtAuthenticationFilter 存入 details）
 */
@Slf4j
@Component
public class SecurityUtil {

    /**
     * 获取当前登录用户的 ID（无需查数据库，直接从 JWT 获取）
     */
    public Integer getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getDetails() instanceof Integer) {
                return (Integer) authentication.getDetails();
            }
        } catch (Exception e) {
            log.error("获取当前用户 ID 失败", e);
        }
        return null;
    }

    /**
     * 获取当前登录用户的手机号
     */
    public String getCurrentUserPhone() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return userDetails.getUsername();
        }
        log.warn("无法从 Security Context 中获取用户信息");
        return null;
    }
}
