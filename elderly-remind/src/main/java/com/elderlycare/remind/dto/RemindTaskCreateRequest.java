package com.elderlycare.remind.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 提醒任务创建请求
 *
 * @author 郑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemindTaskCreateRequest {

    /** 老人 ID */
    @NotNull(message = "老人 ID 不能为空")
    private Integer elderId;

    /** 药品 ID */
    private Long medicineId;

    /** 提醒标题 */
    @NotBlank(message = "提醒标题不能为空")
    private String title;

    /** 提醒内容 */
    private String content;

    /** 提醒时间 */
    private LocalTime remindTime;

    /** 提醒日期 */
    private LocalDate remindDate;

    /** 提醒类型 */
    @NotNull(message = "提醒类型不能为空")
    private Integer remindType;

    /** 是否需要语音播报 (0-否 1-是) */
    private Integer needVoice;

    /** 是否需要弹窗提醒 (0-否 1-是) */
    private Integer needPopup;

    /** 语音播报文本 */
    private String voiceText;

    /** 备注 */
    private String remark;

    /** 重复周期 */
    private Integer repeatCycle;

    /** 结束日期 */
    private LocalDate endDate;
}
