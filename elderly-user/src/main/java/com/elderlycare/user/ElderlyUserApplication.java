package com.elderlycare.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 用户服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.elderlycare.user", "com.elderlycare.common"})
@MapperScan("com.elderlycare.user.mapper")
@EnableDiscoveryClient
@EnableFeignClients
public class ElderlyUserApplication {

    /**
     * 用户服务入口
     */
    public static void main(String[] args) {
        SpringApplication.run(ElderlyUserApplication.class, args);
    }
}
