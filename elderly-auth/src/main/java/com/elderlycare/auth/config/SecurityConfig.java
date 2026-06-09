package com.elderlycare.auth.config;

import com.elderlycare.common.security.config.BaseSecurityConfig;
import com.elderlycare.common.security.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Configuration;

/**
 * Auth 服务安全配置
 */
@Configuration
public class SecurityConfig extends BaseSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 注册JWT认证过滤器
     */
    @Override
    protected JwtAuthenticationFilter jwtAuthenticationFilter() {
        return jwtAuthenticationFilter;
    }


    /**
     * 返回认证服务允许匿名访问的路径列表
     */
    @Override
    protected String[] permitAllPaths() {
        return new String[]{"/auth/sms", "/auth/register", "/auth/login", "/auth/password/reset", "/auth/user/**","/user/elder/create","/user/elder/byUserId"};
    }
}
