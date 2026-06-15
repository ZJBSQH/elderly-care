package com.elderlycare.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息视图对象（对外输出）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    private Integer id;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 姓名
     */
    private String name;
    
    /**
     * 头像
     */
    private String avatar;
    
    /**
     * 年龄
     */
    private Integer age;
    
    /**
     * 用户类型（0:老人 1:家属）
     */
    private Integer userType;
    
    /**
     * 状态（0:禁用 1:启用）
     */
    private Integer status;
    
    /**
     * 性别
     */
    private String sex;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
