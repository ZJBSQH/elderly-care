package com.elderlycare.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 老年护理云平台网关服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ElderlyGatewayApplication {

    /**
     * 启动网关服务
     */
    public static void main(String[] args) {
        SpringApplication.run(ElderlyGatewayApplication.class, args);
    }

}
