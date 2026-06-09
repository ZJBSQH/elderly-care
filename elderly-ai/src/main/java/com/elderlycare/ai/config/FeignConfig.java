package com.elderlycare.ai.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign 客户端配置
 * <p>
 * 为 Feign 出站请求注入 Authorization 头，
 * 将当前用户的 JWT Token 转发到下游微服务，
 * 确保跨服务调用时的认证连续性。
 *
 * @author 郑
 */
@Slf4j
@Configuration
public class FeignConfig {

    /**
     * Feign 请求拦截器 —— 自动转发 JWT Token
     * <p>
     * 从当前 HTTP 请求的 Authorization 头中提取 Bearer Token，
     * 注入到每一个 Feign 出站请求中，使下游服务能够识别当前用户身份。
     * 当前请求无 Token 时（如未登录场景），跳过注入，不做报错处理。
     *
     * @return RequestInterceptor 实例
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return (RequestTemplate template) -> {
            // 从 Spring 的 RequestContextHolder 获取当前 HTTP 请求
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }

            HttpServletRequest request = attributes.getRequest();
            String authHeader = request.getHeader("Authorization");

            // 仅当存在有效的 Bearer Token 时才转发
            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                template.header("Authorization", authHeader);
                log.debug("Feign 请求已转发 Authorization 头到: {}", template.url());
            }
        };
    }
}
