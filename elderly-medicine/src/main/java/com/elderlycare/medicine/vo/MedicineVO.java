package com.elderlycare.medicine.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 用药计划视图对象
 *
 * @author 郑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicineVO {

    /**
     * 主键 ID
     */
    private Integer id;

    /**
     * 老人 ID
     */
    private Integer elderId;

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

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否公共药品: 0-个人, 1-公共
     */
    private Integer isPublic;
}
