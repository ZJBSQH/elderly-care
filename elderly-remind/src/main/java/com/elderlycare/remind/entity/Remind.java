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

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("user_id")
    private Integer userId;

    @TableField("ringtone")
    private String ringtone;

    @TableField("volume")
    private Integer volume;

    @TableField("repeat_mode")
    private String repeatMode;

    @TableField("quiet_time")
    private String quietTime;
}
