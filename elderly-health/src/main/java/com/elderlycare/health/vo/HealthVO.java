package com.elderlycare.health.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 健康记录视图对象
 *
 * @author 郑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthVO {

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 老人ID
     */
    private Integer elderId;

    /**
     * 血压
     */
    private String bloodPressure;

    /**
     * 血糖
     */
    private Double bloodSugar;

    /**
     * 心率
     */
    private Integer heartRate;

    /**
     * 体重
     */
    private Double weight;

    /**
     * 预警标识: 0-正常, 1-异常
     */
    private Integer warningFlag;

    /**
     * 记录时间
     */
    private LocalDateTime recordTime;
}
