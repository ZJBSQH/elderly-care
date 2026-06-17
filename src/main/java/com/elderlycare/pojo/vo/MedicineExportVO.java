package com.elderlycare.pojo.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用药清单导出 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicineExportVO {

    private Integer id;

    private String medicineName;

    private String dosage;

    private String frequency;

    private LocalDate remindDate;

    private LocalDateTime recordTime;

    private Integer status;

    private String statusText;

    private String remark;
}
