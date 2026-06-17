package com.elderlycare.pojo.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicineVO {
    private Integer id;
    private Integer elderId;
    private String medicineName;
    private String dosage;
    private LocalTime remindTime;
    private String frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isPublic;
}
