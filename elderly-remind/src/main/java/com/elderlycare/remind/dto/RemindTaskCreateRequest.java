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

    @NotNull(message = "老人 ID 不能为空")
    private Integer elderId;

    private Long medicineId;

    @NotBlank(message = "提醒标题不能为空")
    private String title;

    private String content;

    @NotNull(message = "提醒时间不能为空")
    private LocalTime remindTime;

    private LocalDate remindDate;

    @NotNull(message = "提醒类型不能为空")
    private Integer remindType;

    private Integer needVoice;

    private Integer needPopup;

    private String voiceText;

    private String remark;

    private Integer repeatCycle;

    private LocalDate endDate;
}
