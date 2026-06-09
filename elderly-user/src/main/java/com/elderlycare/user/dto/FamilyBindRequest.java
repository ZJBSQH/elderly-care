package com.elderlycare.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 家属绑定老人请求（通过手机号绑定）
 */
@Data
public class FamilyBindRequest {

    /** 家属手机号 */
    @NotBlank(message = "家属手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String familyPhone;

    /** 老人手机号 */
    @NotBlank(message = "老人手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String elderPhone;
}
