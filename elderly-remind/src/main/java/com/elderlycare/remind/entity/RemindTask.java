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

    /** 任务 ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 用户 ID */
    @TableField("user_id")
    private Integer userId;

    /** 药品 ID */
    @TableField("medicine_id")
    private Long medicineId;

    /** 老人 ID */
    @TableField("elder_id")
    private Integer elderId;

    /** 提醒标题 */
    @TableField("title")
    private String title;

    /** 提醒内容 */
    @TableField("content")
    private String content;

    /** 提醒时间 */
    @TableField("remind_time")
    private LocalTime remindTime;

    /** 提醒日期 */
    @TableField("remind_date")
    private LocalDate remindDate;

    /** 提醒类型 */
    @TableField("remind_type")
    private Integer remindType;

    /** 是否需要语音播报 (0-否 1-是) */
    @TableField("need_voice")
    private Integer needVoice;

    /** 是否需要弹窗提醒 (0-否 1-是) */
    @TableField("need_popup")
    private Integer needPopup;

    /** 语音播报文本 */
    @TableField("voice_text")
    private String voiceText;

    /** 重复周期 */
    @TableField("repeat_cycle")
    private Integer repeatCycle;

    /** 结束日期 */
    @TableField("end_date")
    private LocalDate endDate;

    /** 状态 (0-禁用 1-启用) */
    @TableField("status")
    private Integer status;

    /** 备注 */
    @TableField("remark")
    private String remark;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
