package com.elderlycare.health.config;

import com.elderlycare.common.security.config.BaseSecurityConfig;
import com.elderlycare.common.security.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Configuration;

/**
 * 健康服务安全配置
 *
 * @author 郑
 */
@Configuration
public class SecurityConfig extends BaseSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 构造方法，注入 JWT 认证过滤器
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 返回 JWT 认证过滤器实例
     */
    @Override
    protected JwtAuthenticationFilter jwtAuthenticationFilter() {
        return jwtAuthenticationFilter;
    }

    /**
     * 健康服务不对外开放路径，所有请求均需认证
     */
    @Override
    protected String[] permitAllPaths() {
        return new String[]{};
    }
}
