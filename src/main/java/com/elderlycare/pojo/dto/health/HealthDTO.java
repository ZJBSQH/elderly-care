package com.elderlycare.pojo.dto.health;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 健康数据录入请求
 */

@Data
public class HealthDTO {

    /**
     * 老人 ID
     */
    @NotNull(message = "老人 ID 不能为空")
    private Integer elderId;

    /**
     * 血压 (格式：120/80)
     */
    private String bloodPressure;

    /**
     * 血糖 (mmol/L)
     */
    private BigDecimal bloodSugar;

    /**
     * 心率 (次/分)
     */
    @Min(30)
    @Max(200)
    private Integer heartRate;

    /**
     * 体重 (kg)
     */
    private BigDecimal weight;
}
