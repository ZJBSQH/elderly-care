package com.elderlycare.pojo.dto.medicine;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用药计划更新请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MedicineUpdateRequest extends BaseMedicineRequest {

    @NotNull (message = "用药计划id不能为空")
    private Integer id;

    private Integer status;

}
