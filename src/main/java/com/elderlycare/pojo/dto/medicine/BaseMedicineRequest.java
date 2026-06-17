package com.elderlycare.pojo.dto.medicine;

import com.elderlycare.pojo.dto.BaseRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 用药请求基类
 * 抽取 MedicineAddRequest、MedicineUpdateRequest 等公共字段
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class BaseMedicineRequest extends BaseRequest {

    @NotBlank(message = "药名不为空")
    private String medicineName;

    @NotBlank(message = "剂量不为空")
    private String dosage;

    private LocalTime remindTime;

    @NotBlank(message = "频次不能为空")
    private String frequency;


    private LocalDate startDate;


    private LocalDate endDate;
}
