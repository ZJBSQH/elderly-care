package com.elderlycare.common.security.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign Token 透传配置
 * 服务间调用时自动转发当前请求的 Authorization 头，保证下游服务还能识别当前用户身份。
 */
@Configuration
public class FeignTokenRelayConfig {

    /**
     * 从当前 HTTP 请求读取 Bearer Token，并写入 Feign 出站请求。
     */
    @Bean
    public RequestInterceptor tokenRelayRequestInterceptor() {
        return (RequestTemplate template) -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }

            HttpServletRequest request = attributes.getRequest();
            String authHeader = request.getHeader("Authorization");
            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                template.header("Authorization", authHeader);
            }
        };
    }
}
