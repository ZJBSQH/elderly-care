// com.elderlycare.common.util.SecurityUtil
package com.elderlycare.common.util;

import com.elderlycare.mapper.UserMapper;
import com.elderlycare.pojo.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Spring Security 相关的工具类
 */
@Slf4j
@Component // 标记为 Spring Bean，以便注入 UserMapper
@RequiredArgsConstructor
public class SecurityUtil {

    private final UserMapper userMapper; // 注入 Mapper 来查询用户信息

    /**
     * 获取当前登录用户的 ID
     * @return 用户 ID，如果获取失败则返回 null
     */
    public Integer getCurrentUserId() {
        try {
            String phone = getCurrentUserPhone();
            if (phone != null) {
                User user = userMapper.selectByPhone(phone);
                if (user != null) {
                    return user.getId();
                } else {
                    log.warn("Security Context 中的手机号在数据库中未找到对应用户: {}", phone);
                }
            }
        } catch (Exception e) {
            log.error("获取当前用户 ID 失败", e);
        }
        return null;
    }

    /**
     * 获取当前登录用户的完整信息
     * @return User 实体，如果获取失败则返回 null
     */
    public User getCurrentUser() {
        try {
            String phone = getCurrentUserPhone();
            if (phone != null) {
                return userMapper.selectByPhone(phone);
            }
        } catch (Exception e) {
            log.error("获取当前用户信息失败", e);
        }
        return null;
    }

    /**
     * 获取当前登录用户的手机号 (用户名)
     * @return 手机号，如果获取失败则返回 null
     */
    private String getCurrentUserPhone() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return userDetails.getUsername(); // 假设用户名就是手机号
        }
        log.warn("无法从 Security Context 中获取用户信息，Authentication: {}", authentication);
        return null;
    }
}
