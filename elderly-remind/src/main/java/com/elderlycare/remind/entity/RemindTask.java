package com.elderlycare.remind.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 提醒任务实体类
 *
 * @author 郑
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
    private Long medicineId;

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

    @TableField("remind_type")
    private Integer remindType;

    @TableField("need_voice")
    private Integer needVoice;

    @TableField("need_popup")
    private Integer needPopup;

    @TableField("voice_text")
    private String voiceText;

    @TableField("repeat_cycle")
    private Integer repeatCycle;

    @TableField("end_date")
    private LocalDate endDate;

    @TableField("status")
    private Integer status;

    @TableField("remark")
    private String remark;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
