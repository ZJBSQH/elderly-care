package com.elderlycare.ai.config;

import com.elderlycare.common.security.config.BaseSecurityConfig;
import com.elderlycare.common.security.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Configuration;

/**
 * AI服务安全配置
 * <p>
 * 继承 {@link BaseSecurityConfig} 公共安全配置，定制 AI 服务的权限策略：
 * AI 问答接口面向所有用户开放，无需 JWT 认证即可访问。
 * 如需对接第三方 AI 平台回调，也应在此配置白名单路径。
 *
 * @author 郑
 */
@Configuration
public class SecurityConfig extends BaseSecurityConfig {

    /** JWT 认证过滤器，由 common-security 模块提供 */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 构造器注入 JWT 过滤器
     *
     * @param jwtAuthenticationFilter JWT 认证过滤器实例
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 提供 JWT 认证过滤器实例
     *
     * @return JwtAuthenticationFilter
     */
    @Override
    protected JwtAuthenticationFilter jwtAuthenticationFilter() {
        return jwtAuthenticationFilter;
    }

    /**
     * 配置无需认证即可访问的路径
     * <p>
     * AI 服务的所有接口（/ai/**）对外暴露，不要求携带 JWT Token。
     *
     * @return 免认证路径数组
     */
    @Override
    protected String[] permitAllPaths() {
        return new String[]{"/ai/**"};
    }
}
