package com.elderlycare.common.security.util;

import com.elderlycare.common.core.exception.BaseErrorCode;
import com.elderlycare.common.core.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Spring Security 工具类
 * 从 SecurityContext 中读取当前登录用户信息，避免业务层重复解析 JWT。
 */
@Slf4j
@Component
public class SecurityUtil {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    /**
     * 获取当前登录用户 ID。
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
     * 获取当前登录用户手机号。
     */
    public String getCurrentUserPhone() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        log.warn("无法从 SecurityContext 中获取用户信息");
        return null;
    }

    /**
     * 判断当前用户是否为管理员。
     */
    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(ROLE_ADMIN::equals);
    }

    /**
     * 要求当前用户必须是管理员，否则抛出 403。
     */
    public void requireAdmin() {
        if (!isAdmin()) {
            throw new BusinessException(BaseErrorCode.FORBIDDEN, "仅管理员可以访问该接口");
        }
    }
}
