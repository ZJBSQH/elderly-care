package com.elderlycare.pojo.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 健康数据视图对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthVO {

    /**
     * 记录 ID
     */
    private Integer id;

    /**
     * 老人 ID
     */
    private Integer elderId;

    /**
     * 血压
     */
    private String bloodPressure;

    /**
     * 血糖
     */
    private BigDecimal bloodSugar;

    /**
     * 心率
     */
    private Integer heartRate;

    /**
     * 体重
     */
    private BigDecimal weight;

    /**
     * 是否异常 (0:正常，1:异常)
     */
    private Integer warningFlag;

    /**
     * 记录时间
     */
    private LocalDateTime recordTime;
}

