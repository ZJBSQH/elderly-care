package com.elderlycare.pojo.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordVO {
    private Integer id;
    private Integer taskId;
    private Integer elderId;
    private LocalDate remindDate;
    private LocalDateTime recordTime;
    private Integer status;
    private String remark;
}
