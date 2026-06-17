package com.elderlycare.pojo.vo.admin;

import java.time.LocalDateTime;

/**
 * 管理员用户信息 VO
 */
public record AdminUserVO(
    /**
     * 用户 ID
     */
    Integer id,

    /**
     * 手机号
     */
    String phone,

    /**
     * 姓名
     */
    String name,

    /**
     * 年龄
     */
    Integer age,

    /**
     * 性别
     */
    String sex,

    /**
     * 头像 URL
     */
    String avatar,

    /**
     * 用户类型：0-老人，1-家属，2-管理员
     */
    Integer userType,

    /**
     * 用户类型描述
     */
    String userTypeDesc,

    /**
     * 状态：0-禁用，1-正常
     */
    Integer status,

    /**
     * 状态描述
     */
    String statusDesc,

    /**
     * 创建时间
     */
    LocalDateTime createTime
) {}
