package com.elderlycare.pojo.dto.record;

import lombok.Data;

import java.time.LocalDate;

/**
 * 用药历史查询请求
 */
@Data
public class RecordHistoryQueryRequest {

    private Integer elderId;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer status;

    private Integer taskId;
    
    // 分页参数
    private Integer pageNum = 1;
    
    private Integer pageSize = 20;
}
