package com.elderlycare.medicine.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 更新用药计划请求
 *
 * @author 郑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicineUpdateRequest {

    /**
     * 用药计划 ID
     */
    @NotNull(message = "用药计划 ID 不能为空")
    private Integer id;

    /**
     * 药品名称
     */
    private String medicineName;

    /**
     * 单次剂量
     */
    private String dosage;

    /**
     * 提醒时间点
     */
    private LocalTime remindTime;

    /**
     * 频次
     */
    private String frequency;

    /**
     * 计划开始日期
     */
    private LocalDate startDate;

    /**
     * 计划结束日期
     */
    private LocalDate endDate;

    /**
     * 状态: 1-进行中, 0-停用
     */
    private Integer status;
}
