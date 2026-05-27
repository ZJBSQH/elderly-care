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

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("task_id")
    private Integer taskId;

    @TableField("user_id")
    private Integer userId;

    @TableField("elder_id")
    private Integer elderId;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("notify_type")
    private Integer notifyType;

    @TableField("send_time")
    private LocalDateTime sendTime;

    @TableField("read_status")
    private Integer readStatus;

    @TableField("read_time")
    private LocalDateTime readTime;

    @TableField("status")
    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
