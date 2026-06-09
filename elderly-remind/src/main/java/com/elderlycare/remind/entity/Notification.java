package com.elderlycare.remind.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知记录实体类
 *
 * @author 郑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("notification")
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 通知 ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 关联任务 ID */
    @TableField("task_id")
    private Integer taskId;

    /** 用户 ID */
    @TableField("user_id")
    private Integer userId;

    /** 老人 ID */
    @TableField("elder_id")
    private Integer elderId;

    /** 通知标题 */
    @TableField("title")
    private String title;

    /** 通知内容 */
    @TableField("content")
    private String content;

    /** 通知类型 */
    @TableField("notify_type")
    private Integer notifyType;

    /** 发送时间 */
    @TableField("send_time")
    private LocalDateTime sendTime;

    /** 读取状态 (0-未读 1-已读) */
    @TableField("read_status")
    private Integer readStatus;

    /** 读取时间 */
    @TableField("read_time")
    private LocalDateTime readTime;

    /** 状态 (0-无效 1-有效) */
    @TableField("status")
    private Integer status;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
