package com.elderlycare.news.config;

import com.elderlycare.common.security.config.BaseSecurityConfig;
import com.elderlycare.common.security.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Configuration;

/**
 * 资讯服务安全配置
 *
 * @author 郑
 */
@Configuration
public class SecurityConfig extends BaseSecurityConfig {

    /** JWT认证过滤器 */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /** 构造器注入JWT过滤器 */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 提供JWT认证过滤器
     */
    @Override
    protected JwtAuthenticationFilter jwtAuthenticationFilter() {
        return jwtAuthenticationFilter;
    }

    /**
     * 放行路径（推荐文章/热门文章/搜索/列表/详情无需登录）
     */
    @Override
    protected String[] permitAllPaths() {
        return new String[]{
            "/health-knowledge/**",
        };
    }
}
