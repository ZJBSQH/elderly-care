package com.elderlycare.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("phone")
    private String phone;

    @TableField("password")
    private String password;

    @TableField("name")
    private String name;

    @TableField("avatar")
    private String avatar;

    @TableField("age")
    private Integer age;

    @TableField("user_type")
    private Integer userType;

    @TableField("status")
    private Integer status;

    @TableField("sex")
    private String sex;

    @TableField(value = "create_time")
    private LocalDateTime createTime;
}
