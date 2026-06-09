package com.elderlycare.health.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 健康记录数据传输对象
 *
 * @author 郑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthDTO {

    /**
     * 老人ID（Controller 层自动从安全上下文填充，允许为空）
     */
    private Integer elderId;

    /**
     * 血压（格式: "120/80"）
     */
    private String bloodPressure;

    /**
     * 血糖（mmol/L）
     */
    private Double bloodSugar;

    /**
     * 心率（次/分钟）
     */
    @Min(value = 30, message = "心率不能低于30次/分钟")
    @Max(value = 200, message = "心率不能超过200次/分钟")
    private Integer heartRate;

    /**
     * 体重（kg）
     */
    private Double weight;
}
