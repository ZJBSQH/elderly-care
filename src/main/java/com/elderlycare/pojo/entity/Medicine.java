package com.elderlycare.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * 药品实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("medicine")
public class Medicine implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID (自增)
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 老人 ID
     */
    @TableField("elder_id")
    private Integer elderId;

    /**
     * 药品名称
     */
    @TableField("medicine_name")
    private String medicineName;

    /**
     * 剂量
     */
    @TableField("dosage")
    private String dosage;

    /**
     * 提醒时间
     */
    @TableField("remind_time")
    private LocalTime remindTime;

    /**
     * 服药频率
     */
    @TableField("frequency")
    private String frequency;

    /**
     * 开始日期
     */
    @TableField("start_date")
    private LocalDate startDate;

    /**
     * 结束日期
     */
    @TableField("end_date")
    private LocalDate endDate;

    /**
     * 状态 (默认 1)
     */
    @TableField("status")
    private Integer status = 1;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    //修改时间
    @TableField(value = "update_time", fill = FieldFill.INSERT)
    private LocalDateTime updateTime;

    //公共药品
    @TableField("is_public")
    private Integer isPublic;
}
