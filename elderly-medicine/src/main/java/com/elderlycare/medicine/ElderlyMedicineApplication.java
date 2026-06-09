package com.elderlycare.medicine;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 用药服务启动类
 *
 * @author 郑
 */
@SpringBootApplication(scanBasePackages = {"com.elderlycare.medicine", "com.elderlycare.common"})
@MapperScan("com.elderlycare.medicine.mapper")
@EnableDiscoveryClient
@EnableFeignClients
public class ElderlyMedicineApplication {

    /**
     * 应用启动入口
     */
    public static void main(String[] args) {
        SpringApplication.run(ElderlyMedicineApplication.class, args);
    }
}
