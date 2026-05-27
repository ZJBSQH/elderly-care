package com.elderlycare.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 家属绑定关系实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("family")
public class Family implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("family_user_id")
    private Integer familyUserId;

    @TableField("elder_id")
    private Integer elderId;

    @TableField("relation")
    private String relation;

    @TableField("phone")
    private String phone;

    @TableField("bind_status")
    private Integer bindStatus;

    @TableField("bind_time")
    private LocalDateTime bindTime;
}
