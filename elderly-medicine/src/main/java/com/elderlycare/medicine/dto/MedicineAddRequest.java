package com.elderlycare.medicine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 添加用药计划请求
 *
 * @author 郑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicineAddRequest {

    /**
     * 老人 ID
     */
    @NotNull(message = "老人 ID 不能为空")
    private Integer elderId;

    /**
     * 药品名称
     */
    @NotBlank(message = "药品名称不能为空")
    private String medicineName;

    /**
     * 单次剂量
     */
    @NotBlank(message = "单次剂量不能为空")
    private String dosage;

    /**
     * 提醒时间点
     */
    private LocalTime remindTime;

    /**
     * 频次
     */
    @NotBlank(message = "频次不能为空")
    private String frequency;

    /**
     * 计划开始日期
     */
    private LocalDate startDate;

    /**
     * 计划结束日期
     */
    private LocalDate endDate;
}
