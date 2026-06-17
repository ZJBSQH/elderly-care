package com.elderlycare.pojo.dto.remind;

// 查询请求

import lombok.Data;

import java.time.LocalDate;

@Data
public class RemindQueryRequest {

    private Integer userId;

    private Integer elderId;

    private Integer remindType;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer status;
}
