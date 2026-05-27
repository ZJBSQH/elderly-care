package com.elderlycare.remind.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 提醒任务更新请求
 *
 * @author 郑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemindTaskUpdateRequest {

    @NotNull(message = "任务 ID 不能为空")
    private Integer id;

    private Long medicineId;

    private String title;

    private String content;

    private LocalTime remindTime;

    private LocalDate remindDate;

    private Integer remindType;

    private Integer needVoice;

    private Integer needPopup;

    private String voiceText;

    private Integer repeatCycle;

    private LocalDate endDate;
}
