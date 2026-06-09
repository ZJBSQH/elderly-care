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

    /** 绑定记录ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 家属认证用户ID */
    @TableField("family_user_id")
    private Integer familyUserId;

    /** 被绑定的老人档案ID */
    @TableField("elder_id")
    private Integer elderId;

    /** 与老人的关系（如：子女、配偶） */
    @TableField("relation")
    private String relation;

    /** 家属联系电话 */
    @TableField("phone")
    private String phone;

    /** 绑定状态（1-已绑定，0-已解绑） */
    @TableField("bind_status")
    private Integer bindStatus;

    /** 绑定时间 */
    @TableField("bind_time")
    private LocalDateTime bindTime;
}
