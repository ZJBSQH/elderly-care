package com.elderlycare.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket 配置类
 * 注意：使用 @ServerEndpoint 注解时，不需要实现 WebSocketConfigurer
 */
@Configuration
public class WebSocketConfig {

    /**
     * 注入 ServerEndpointExporter，自动注册 @ServerEndpoint 端点
     * 如果没有这个 Bean，@ServerEndpoint 注解将失效
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
