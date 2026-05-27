package com.elderlycare.health.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 健康记录实体
 *
 * @author 郑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("health")
public class Health {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 老人ID（软引用 elderly-user 服务）
     */
    @TableField("elder_id")
    private Integer elderId;

    /**
     * 血压（格式: 收缩压/舒张压，如 "120/80"）
     */
    @TableField("blood_pressure")
    private String bloodPressure;

    /**
     * 血糖（mmol/L）
     */
    @TableField("blood_sugar")
    private Double bloodSugar;

    /**
     * 心率（次/分钟）
     */
    @TableField("heart_rate")
    private Integer heartRate;

    /**
     * 体重（kg）
     */
    @TableField("weight")
    private Double weight;

    /**
     * 预警标识: 0-正常, 1-异常
     */
    @TableField("warning_flag")
    private Integer warningFlag;

    /**
     * 是否已读: 0-未读, 1-已读（仅对预警记录有意义）
     */
    @TableField("is_read")
    private Integer isRead;

    /**
     * 记录时间
     */
    @TableField(value = "record_time", fill = FieldFill.INSERT)
    private LocalDateTime recordTime;
}
