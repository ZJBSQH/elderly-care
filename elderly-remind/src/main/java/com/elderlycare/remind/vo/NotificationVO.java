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
    /** 通知 ID */
    private Integer id;
    /** 关联任务 ID */
    private Integer taskId;
    /** 用户 ID */
    private Integer userId;
    /** 老人 ID */
    private Integer elderId;
    /** 通知标题 */
    private String title;
    /** 通知内容 */
    private String content;
    /** 通知类型 */
    private Integer notifyType;
    /** 发送时间 */
    private LocalDateTime sendTime;
    /** 读取状态 (0-未读 1-已读) */
    private Integer readStatus;
    /** 读取时间 */
    private LocalDateTime readTime;
    /** 状态 (0-无效 1-有效) */
    private Integer status;
    /** 创建时间 */
    private LocalDateTime createTime;
}
