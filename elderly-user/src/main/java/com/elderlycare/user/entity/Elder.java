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

    /** 老人档案ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /** 关联的认证用户ID */
    @TableField("user_id")
    private Integer userId;

    /** 病史描述 */
    @TableField("medical_history")
    private String medicalHistory;

    /** 过敏药物列表 */
    @TableField("allergic_drugs")
    private String allergicDrugs;

    /** 紧急联系人信息 */
    @TableField("emergency_contact")
    private String emergencyContact;

    /** 健康档案文件路径 */
    @TableField("health_file")
    private String healthFile;

    /** 二维码Token（用于家属扫码绑定） */
    @TableField("qr_code_token")
    private String qrCodeToken;
}
