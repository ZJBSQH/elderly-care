package com.elderlycare.remind.config;

import com.elderlycare.common.security.config.BaseSecurityConfig;
import com.elderlycare.common.security.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Configuration;

/**
 * Remind 服务安全配置
 *
 * @author 郑
 */
@Configuration
public class SecurityConfig extends BaseSecurityConfig {

    /** JWT 认证过滤器 */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Override
    protected JwtAuthenticationFilter jwtAuthenticationFilter() {
        return jwtAuthenticationFilter;
    }

    @Override
    protected String[] permitAllPaths() {
        return new String[]{"/ws/**", "/remind/**"};
    }
}
