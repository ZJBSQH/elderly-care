package com.elderlycare.pojo.dto.medicine;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用药计划修改请求（家属远程修改）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MedicineRemoteUpdateRequest extends BaseMedicineRequest {

    @NotNull(message = "用药计划 ID 不能为空")
    private Integer medicineId;

    private Integer status;
}
