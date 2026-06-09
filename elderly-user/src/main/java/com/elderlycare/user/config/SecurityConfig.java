package com.elderlycare.user.config;

import com.elderlycare.common.security.config.BaseSecurityConfig;
import com.elderlycare.common.security.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Configuration;

/**
 * 用户服务安全配置（继承公共安全配置，定义放行路径）
 */
@Configuration
public class SecurityConfig extends BaseSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 构造注入JWT认证过滤器
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 返回JWT认证过滤器实例
     */
    @Override
    protected JwtAuthenticationFilter jwtAuthenticationFilter() {
        return jwtAuthenticationFilter;
    }

    /**
     * 定义无需认证即可访问的路径
     */
    @Override
    protected String[] permitAllPaths() {
        return new String[]{"/user/elder/qrcode/**", "/user/elder/byUserId", "/user/elder/create"};
    }
}
