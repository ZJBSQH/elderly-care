package com.elderlycare.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 老人健康档案实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("elder")
public class Elder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("user_id")
    private Integer userId;

    @TableField("medical_history")
    private String medicalHistory;

    @TableField("allergic_drugs")
    private String allergicDrugs;

    @TableField("emergency_contact")
    private String emergencyContact;

    @TableField("health_file")
    private String healthFile;

    @TableField("qr_code_token")
    private String qrCodeToken;
}
