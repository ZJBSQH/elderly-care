package com.elderlycare.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("family")
public class Family implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID (自增)
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 家庭成员用户 ID
     */
    @TableField("family_user_id")
    private Integer familyUserId;

    /**
     * 老人 ID
     */
    @TableField("elder_id")
    private Integer elderId;

    @TableField("relation")
    private String relation;

    @TableField("phone")
    private String phone;
    /**
     * 绑定状态
     */
    @TableField("bind_status")
    private Integer bindStatus;

    @TableField("bind_time")
    private LocalDateTime bindTime;
}

