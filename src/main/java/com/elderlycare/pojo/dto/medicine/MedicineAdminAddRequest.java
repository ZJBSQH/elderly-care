package com.elderlycare.pojo.dto.medicine;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理员添加药品请求（公共药库）
 * 统一管理用户端和管理端的药品添加
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MedicineAdminAddRequest extends BaseMedicineRequest {

    /**
     * 是否公共药品：0-个人用药，1-公共药品
     */
    private Integer isPublic = 1;
}
