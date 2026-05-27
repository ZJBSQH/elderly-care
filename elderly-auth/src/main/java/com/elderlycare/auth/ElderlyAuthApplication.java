package com.elderlycare.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.elderlycare.auth", "com.elderlycare.common"})
@MapperScan("com.elderlycare.auth.mapper")
@EnableDiscoveryClient
@EnableFeignClients
public class ElderlyAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElderlyAuthApplication.class, args);
    }
}
