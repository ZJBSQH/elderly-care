package com.elderlycare.remind.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知记录视图对象
 *
 * @author 郑
 */
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
