package com.elderlycare.medicine.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 用药计划实体
 *
 * @author 郑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("medicine")
public class Medicine {

    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 关联老人 ID (软引用 db_elderly_user.elder.id)
     */
    @TableField("elder_id")
    private Integer elderId;

    /**
     * 药品名称
     */
    @TableField("medicine_name")
    private String medicineName;

    /**
     * 单次剂量
     */
    @TableField("dosage")
    private String dosage;

    /**
     * 提醒时间点
     */
    @TableField("remind_time")
    private LocalTime remindTime;

    /**
     * 频次
     */
    @TableField("frequency")
    private String frequency;

    /**
     * 计划开始日期
     */
    @TableField("start_date")
    private LocalDate startDate;

    /**
     * 计划结束日期
     */
    @TableField("end_date")
    private LocalDate endDate;

    /**
     * 状态: 1-进行中, 0-停用
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 是否公共药品: 0-个人, 1-公共
     */
    @TableField("is_public")
    private Integer isPublic;
}
