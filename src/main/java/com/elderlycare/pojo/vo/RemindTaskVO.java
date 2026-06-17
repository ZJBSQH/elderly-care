package com.elderlycare.pojo.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemindTaskVO {
    private Integer id;
    private Integer userId;
    private Integer medicineId;
    private Integer elderId;
    private String title;
    private String content;
    private LocalTime remindTime;
    private LocalDate remindDate;
    private Integer remindType;
    private Boolean needVoice;
    private Boolean needPopup;
    private String voiceText;
    private Integer repeatCycle;
    private LocalDate endDate;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
