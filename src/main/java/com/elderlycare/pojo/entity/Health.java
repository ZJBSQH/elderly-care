package com.elderlycare.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("health")
public class Health implements Serializable {

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
     * 血压
     */
    @TableField("blood_pressure")
    private String bloodPressure;

    /**
     * 血糖
     */
    @TableField("blood_sugar")
    private BigDecimal bloodSugar;

    /**
     * 心率
     */
    @TableField("heart_rate")
    private Integer heartRate;

    /**
     * 体重
     */
    @TableField("weight")
    private BigDecimal weight;

    /**
     * 警告标志 (0:正常，1:异常)
     */
    @TableField("warning_flag")
    private Integer warningFlag = 0;

    /**
     * 记录时间
     */
    @TableField(value = "record_time", fill = FieldFill.INSERT)
    private LocalDateTime recordTime;
}
