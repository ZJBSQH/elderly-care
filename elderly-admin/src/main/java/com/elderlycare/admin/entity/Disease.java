package com.elderlycare.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 疾病字典实体
 *
 * @author 郑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("disease")
public class Disease {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

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
     * 状态: 0-禁用, 1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
