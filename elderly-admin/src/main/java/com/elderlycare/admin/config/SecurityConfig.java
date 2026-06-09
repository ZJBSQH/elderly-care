package com.elderlycare.admin.config;

import com.elderlycare.common.security.config.BaseSecurityConfig;
import com.elderlycare.common.security.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Configuration;

/**
 * 后台管理服务安全配置
 *
 * @author 郑
 */
@Configuration
public class SecurityConfig extends BaseSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 构造函数注入JWT认证过滤器
     *
     * @param jwtAuthenticationFilter JWT认证过滤器
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 提供JWT认证过滤器
     *
     * @return JWT认证过滤器实例
     */
    @Override
    protected JwtAuthenticationFilter jwtAuthenticationFilter() {
        return jwtAuthenticationFilter;
    }

    /**
     * 返回无需认证即可访问的路径
     *
     * @return 公开访问路径数组（后台管理无公开路径）
     */
    @Override
    protected String[] permitAllPaths() {
        return new String[]{};
    }
}
