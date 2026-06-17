package com.elderlycare.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * 提醒设置实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("remind")
public class Remind implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID (自增)
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户 ID
     */
    @TableField("user_id")
    private Integer userId;

    /**
     * 铃声
     */
    @TableField("ringtone")
    private String ringtone;

    /**
     * 音量
     */
    @TableField("volume")
    private Integer volume;

    /**
     * 重复模式
     */
    @TableField("repeat_mode")
    private String repeatMode;

    /**
     * 安静时间
     */
    @TableField("quiet_time")
    private String quietTime;
}

