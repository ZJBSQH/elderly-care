package com.elderlycare.remind.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 提醒设置实体类
 *
 * @author 郑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("remind")
public class Remind implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 提醒设置 ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 用户 ID */
    @TableField("user_id")
    private Integer userId;

    /** 铃声 */
    @TableField("ringtone")
    private String ringtone;

    /** 音量 */
    @TableField("volume")
    private Integer volume;

    /** 重复模式 */
    @TableField("repeat_mode")
    private String repeatMode;

    /** 免打扰时间段 */
    @TableField("quiet_time")
    private String quietTime;
}
