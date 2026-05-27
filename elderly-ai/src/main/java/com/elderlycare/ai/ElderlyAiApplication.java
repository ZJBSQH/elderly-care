package com.elderlycare.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * AI助手服务启动类
 * 无数据库，通过Feign调用medicine和remind服务，集成Spring AI Alibaba DashScope实现AI问答
 *
 * @author 郑
 */
@SpringBootApplication(scanBasePackages = {"com.elderlycare.ai", "com.elderlycare.common"})
@EnableDiscoveryClient
@EnableFeignClients
public class ElderlyAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElderlyAiApplication.class, args);
    }
}
