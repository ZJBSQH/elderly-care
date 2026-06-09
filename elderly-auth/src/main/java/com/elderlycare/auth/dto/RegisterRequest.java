package com.elderlycare.auth.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 注册请求参数
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "验证码不能为空")
    private String smsCode;

    private String sex;

    @Min(value = 1, message = "年龄不能小于1")
    @Max(value = 120, message = "年龄不能大于120")
    private Integer age;

    private String name;

    @Min(value = 0, message = "用户类型错误")
    @Max(value = 1, message = "用户类型错误")
    private Integer userType;
}
