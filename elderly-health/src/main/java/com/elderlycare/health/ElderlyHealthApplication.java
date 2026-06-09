package com.elderlycare.health;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 健康管理服务启动类
 *
 * @author 郑
 */
@SpringBootApplication(scanBasePackages = {"com.elderlycare.health", "com.elderlycare.common"})
@MapperScan("com.elderlycare.health.mapper")
@EnableDiscoveryClient
@EnableFeignClients
public class ElderlyHealthApplication {
    /**
     * 健康管理服务启动入口
     */
    public static void main(String[] args) {
        SpringApplication.run(ElderlyHealthApplication.class, args);
    }
}
