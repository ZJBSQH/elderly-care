package com.elderlycare.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * AI助手服务启动类
 * <p>
 * 老年护理云平台AI助手微服务，作为无状态聚合服务：
 * <ul>
 *   <li>无独立数据库，通过 Feign 调用 medicine / remind 服务获取数据</li>
 *   <li>集成 Spring AI Alibaba DashScope 实现 AI 问答（当前为模拟实现）</li>
 *   <li>通过 SSE（Server-Sent Events）流式返回 AI 回答</li>
 * </ul>
 *
 * @author 郑
 */
@SpringBootApplication(scanBasePackages = {"com.elderlycare.ai", "com.elderlycare.common"}) // 扫描本模块及公共模块
@EnableDiscoveryClient  // 启用 Nacos 服务注册与发现
@EnableFeignClients     // 启用 OpenFeign 声明式跨服务调用
public class ElderlyAiApplication {

    /**
     * 应用主入口
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ElderlyAiApplication.class, args);
    }
}
