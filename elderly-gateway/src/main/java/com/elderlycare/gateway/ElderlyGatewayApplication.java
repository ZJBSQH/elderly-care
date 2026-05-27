package com.elderlycare.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ElderlyGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElderlyGatewayApplication.class, args);
    }

}
