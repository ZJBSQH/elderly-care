package com.elderlycare.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProfileUpdateRequest {

    @NotNull(message = "用户ID不能为空")
    private Integer id;

    private String name;
    private Integer age;
    private String sex;
    private String avatar;
}
