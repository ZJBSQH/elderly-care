package com.elderlycare.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.elderlycare.user", "com.elderlycare.common"})
@MapperScan("com.elderlycare.user.mapper")
@EnableDiscoveryClient
@EnableFeignClients
public class ElderlyUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElderlyUserApplication.class, args);
    }
}
