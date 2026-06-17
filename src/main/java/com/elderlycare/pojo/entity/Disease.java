package com.elderlycare.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 疾病实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("disease")
public class Disease implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 疾病名称
     */
    @TableField("disease_name")
    private String diseaseName;

    /**
     * 疾病分类
     */
    @TableField("category")
    private String category;

    /**
     * 症状描述
     */
    @TableField("symptoms")
    private String symptoms;

    /**
     * 治疗方法
     */
    @TableField("treatment")
    private String treatment;

    /**
     * 预防建议
     */
    @TableField("prevention")
    private String prevention;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status = 1;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
