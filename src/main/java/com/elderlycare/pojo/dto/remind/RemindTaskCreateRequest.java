package com.elderlycare.pojo.dto.remind;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 创建提醒任务请求参数
 *
 */
@Data
public class RemindTaskCreateRequest {

    private Integer userId;

    @NotNull(message = "老人ID不为空")
    private Integer elderId;

    private Integer medicineId;

    @NotBlank(message = "任务名称不能为空")
    private String title;

    private String content;

    @NotNull(message = "提醒时间不能为空")
    private LocalTime remindTime;

    private LocalDate remindDate;

    @NotNull(message = "提醒类型不能为空")
    private Integer remindType;

    private Boolean needVoice = true;

    private Boolean needPopup = true;

    private String voiceText;

    private  String remark;

    // 重复周期
    private Integer repeatCycle;

    private LocalDate endDate;
}
