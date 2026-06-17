package com.elderlycare.pojo.dto.remind;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;


/**
 * 创建提醒任务请求参数
 *
 */
@Data
public class RemindTaskUpdateRequest {


    @NotNull(message = "老人ID不为空")
    private Integer id;

    private Integer medicineId;

    private String title;

    private String content;

    private LocalTime remindTime;

    private LocalDate remindDate;


    private Integer remindType;

    private Boolean needVoice = true;

    private Boolean needPopup = true;

    private  String voiceText;

    // 重复周期
    private Integer repeatCycle;

    private LocalDate endDate;
}
