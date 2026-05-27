package com.elderlycare.remind;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 提醒服务启动类
 * 端口 8085，数据库 db_elderly_remind
 *
 * @author 郑
 */
@SpringBootApplication(scanBasePackages = {"com.elderlycare.remind", "com.elderlycare.common"})
@MapperScan("com.elderlycare.remind.mapper")
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
public class ElderlyRemindApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElderlyRemindApplication.class, args);
    }
}
