package com.elderlycare.medicine.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 服药记录实体
 *
 * @author 郑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("record")
public class Record {

    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 关联用药计划 ID
     */
    @TableField("task_id")
    private Integer taskId;

    /**
     * 老人 ID (软引用 db_elderly_user.elder.id)
     */
    @TableField("elder_id")
    private Integer elderId;

    /**
     * 计划服药日期
     */
    @TableField("remind_date")
    private LocalDate remindDate;

    /**
     * 实际服药时间
     */
    @TableField("record_time")
    private LocalDateTime recordTime;

    /**
     * 状态: 0-待服, 1-已服, 2-漏服, 3-跳过
     */
    @TableField("status")
    private Integer status;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
}
