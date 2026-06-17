package com.elderlycare.pojo.dto.medicine;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 老人用药计划
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MedicineAddRequest extends BaseMedicineRequest {

    @NotNull(message = "老人id不为空")
    private Integer elderId;

}
