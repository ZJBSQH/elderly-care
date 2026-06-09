package com.elderlycare.news;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 老年护理 - 资讯服务启动类
 *
 * @author 郑
 */
@SpringBootApplication(scanBasePackages = {"com.elderlycare.news", "com.elderlycare.common"})
@MapperScan("com.elderlycare.news.mapper")
@EnableDiscoveryClient
@EnableFeignClients
public class ElderlyNewsApplication {
    /**
     * Spring Boot应用入口
     */
    public static void main(String[] args) {
        SpringApplication.run(ElderlyNewsApplication.class, args);
    }
}
