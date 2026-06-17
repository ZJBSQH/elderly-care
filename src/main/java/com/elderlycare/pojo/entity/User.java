package com.elderlycare.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID (自增)
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 密码
     */
    @TableField("password")
    private String password;

    /**
     * 姓名
     */
    @TableField("name")
    private String name;

    //头像
    @TableField("avatar")
    private String avatar;

    /**
     * 年龄
     */
    @TableField("age")
    private Integer age;

    /**
     * 角色
     */
    @TableField("user_type")
    private Integer userType;

    /**
     * 状态 (默认 1)
     */
    @TableField("status")
    private Integer status;

    @TableField("sex")
    private String sex;

    @TableField(value = "create_time")
    private LocalDateTime createTime;
}
