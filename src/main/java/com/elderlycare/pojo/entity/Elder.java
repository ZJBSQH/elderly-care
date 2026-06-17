package com.elderlycare.pojo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * 健康档案实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("elder")
public class Elder implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID (自增)
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;


    @TableField("user_id")
    private Integer userId;

    /**
     * 病史
     */
    @TableField("medical_history")
    private String medicalHistory;

    /**
     * 过敏药物
     */
    @TableField("allergic_drugs")
    private String allergicDrugs;

    /**
     * 紧急联系人
     */
    @TableField("emergency_contact")
    private String emergencyContact;

    /**
     * 健康档案文件
     */
    @TableField("health_file")
    private String healthFile;

    /**
     * 二维码唯一标识
     */
    @TableField("qr_code_token")
    private String qrCodeToken;
}

