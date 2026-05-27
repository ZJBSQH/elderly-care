package com.elderlycare.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 疾病字典 VO
 *
 * @author 郑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiseaseVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 疾病名称
     */
    private String diseaseName;

    /**
     * 疾病分类
     */
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

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
