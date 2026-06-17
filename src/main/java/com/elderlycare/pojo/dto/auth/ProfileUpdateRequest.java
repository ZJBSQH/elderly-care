package com.elderlycare.pojo.dto.auth;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProfileUpdateRequest {

    @NotNull(message = "用户不能为空")
    private Integer id;

    private String name;

    private Integer age;

    private String sex;
    // 头像
    private String avatar;

}
