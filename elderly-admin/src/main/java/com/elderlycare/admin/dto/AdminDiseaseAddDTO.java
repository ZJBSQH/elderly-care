package com.elderlycare.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 疾病添加 DTO
 *
 * @author 郑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;
}
