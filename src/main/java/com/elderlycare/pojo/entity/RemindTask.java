package com.elderlycare.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 提醒任务实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("remind_task")
public class RemindTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("user_id")
    private Integer userId;

    @TableField("medicine_id")
    private Integer medicineId;

    @TableField("elder_id")
    private Integer elderId;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("remind_time")
    private LocalTime remindTime;

    @TableField("remind_date")
    private LocalDate remindDate;

    //  // 1-用药，2-体检，3-活动，4-其他
    @TableField("remind_type")
    private Integer remindType;

    @TableField("need_voice")
    private Boolean needVoice = true;

    @TableField("need_popup")
    private Boolean needPopup = true;

    @TableField("voice_text")
    private String voiceText;

    @TableField("repeat_cycle")
    private Integer repeatCycle;  // 0-不重复，1-每天，2-每周，3-每月


    @TableField("end_date")
    private LocalDate endDate;

    @TableField("status")
    private Integer status = 1;

    @TableField("remark")
    private String remark;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
