package com.elderlycare.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 后台管理服务启动类
 *
 * @author 郑
 */
@SpringBootApplication(scanBasePackages = {"com.elderlycare.admin", "com.elderlycare.common"})
@MapperScan("com.elderlycare.admin.mapper")
@EnableDiscoveryClient
@EnableFeignClients
public class ElderlyAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElderlyAdminApplication.class, args);
    }
}
