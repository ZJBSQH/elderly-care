package com.elderlycare.pojo.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationVO {
    private Integer id;
    private Integer taskId;
    private Integer userId;
    private Integer elderId;
    private String title;
    private String content;
    private Integer notifyType;
    private LocalDateTime sendTime;
    private Integer readStatus;
    private LocalDateTime readTime;
    private Integer status;
    private LocalDateTime createTime;
}
