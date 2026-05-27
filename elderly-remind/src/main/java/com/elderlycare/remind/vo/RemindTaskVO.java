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
    private Integer id;
    private Integer userId;
    private Long medicineId;
    private Integer elderId;
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
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
