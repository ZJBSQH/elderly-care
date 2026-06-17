package com.elderlycare.pojo.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 管理员更新用户信息 DTO
 */
@Data
public class AdminUserUpdateDTO {

    /**
     * 姓名
     */
    @Pattern(regexp = "^[\u4e00-\u9fa5a-zA-Z ]{1,50}$", message = "姓名格式不正确")
    private String name;

    /**
     * 年龄
     */
    @Min(value = 0, message = "年龄最小为 0")
    @Max(value = 150, message = "年龄最大为 150")
    private Integer age;

    /**
     * 性别
     */
    @Pattern(regexp = "^[男女]$", message = "性别只能是男或女")
    private String sex;

    /**
     * 头像 URL
     */
    private String avatar;

    /**
     * 用户类型：0-老人，1-家属，2-管理员
     */
    @Min(value = 0, message = "用户类型最小值为 0")
    @Max(value = 2, message = "用户类型最大值为 2")
    private Integer userType;

    /**
     * 状态：0-禁用，1-正常
     */
    @Min(value = 0, message = "状态最小值为 0")
    @Max(value = 1, message = "状态最大值为 1")
    private Integer status;
}
