package com.elderlycare.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 服药记录实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("record")
public class Record implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID (自增)
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 计划 ID
     */
    @TableField("task_id")
    private Integer taskId;

    /**
     * 老人 ID
     */
    @TableField("elder_id")
    private Integer elderId;

    /**
     * 计划日期
     */
    @TableField("remind_date")
    private LocalDate remindDate;

    /**
     * 服药时间
     */
    @TableField("record_time")
    private LocalDateTime recordTime;

    /**
     * 状态 (0:未服用，1:已服用)
     */
    @TableField("status")
    private Integer status = 0;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
}
