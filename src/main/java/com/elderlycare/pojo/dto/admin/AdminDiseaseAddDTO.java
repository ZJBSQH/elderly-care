package com.elderlycare.pojo.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理员添加疾病 DTO
 */
@Data
public class AdminDiseaseAddDTO {

    /**
     * 疾病名称
     */
    @NotBlank(message = "疾病名称不能为空")
    private String diseaseName;

    /**
     * 疾病分类
     */
    @NotBlank(message = "疾病分类不能为空")
    private String category;

    /**
     * 症状描述
     */
    private String symptoms;

    /**
     * 治疗方法
     */
    private String treatment;

    /**
     * 预防建议
     */
    private String prevention;

    /**
     * 状态：0-禁用，1-启用
     */
    @NotNull(message = "状态不能为空")
    private Integer status = 1;
}
