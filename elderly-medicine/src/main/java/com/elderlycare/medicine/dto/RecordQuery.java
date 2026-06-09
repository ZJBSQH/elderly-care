package com.elderlycare.medicine.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 服药记录查询参数
 *
 * @author 郑
 */
@Data
public class RecordQuery {

    /** 老人ID（不传则从 JWT 安全上下文获取） */
    private Integer elderId;

    /** 页码（默认 1） */
    private Integer pageNum = 1;

    /** 每页大小（默认 20） */
    private Integer pageSize = 20;

    /** 开始日期（可选，格式 yyyy-MM-dd） */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /** 结束日期（可选，格式 yyyy-MM-dd） */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /** 服药状态（可选，1-已服 2-漏服） */
    private Integer status;
}
