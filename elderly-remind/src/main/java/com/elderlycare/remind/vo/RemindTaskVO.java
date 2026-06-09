package com.elderlycare.remind.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 提醒任务视图对象
 *
 * @author 郑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemindTaskVO {
    /** 任务 ID */
    private Integer id;
    /** 用户 ID */
    private Integer userId;
    /** 药品 ID */
    private Long medicineId;
    /** 老人 ID */
    private Integer elderId;
    /** 提醒标题 */
    private String title;
    /** 提醒内容 */
    private String content;
    /** 提醒时间 */
    private LocalTime remindTime;
    /** 提醒日期 */
    private LocalDate remindDate;
    /** 提醒类型 */
    private Integer remindType;
    /** 是否需要语音播报 (0-否 1-是) */
    private Integer needVoice;
    /** 是否需要弹窗提醒 (0-否 1-是) */
    private Integer needPopup;
    /** 语音播报文本 */
    private String voiceText;
    /** 重复周期 */
    private Integer repeatCycle;
    /** 结束日期 */
    private LocalDate endDate;
    /** 状态 (0-禁用 1-启用) */
    private Integer status;
    /** 备注 */
    private String remark;
    /** 创建时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
