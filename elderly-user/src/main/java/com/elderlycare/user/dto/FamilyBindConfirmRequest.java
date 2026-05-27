package com.elderlycare.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 家属绑定老人确认请求（二维码确认）
 */
@Data
public class FamilyBindConfirmRequest {

    @NotNull(message = "老人ID不能为空")
    private Integer elderId;

    @NotBlank(message = "关系不能为空")
    private String relation;
}
