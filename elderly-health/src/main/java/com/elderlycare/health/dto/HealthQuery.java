package com.elderlycare.health.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 健康记录查询参数
 *
 * @author 郑
 */
@Data
public class HealthQuery {

    /** 老人ID（不传则从 JWT 安全上下文获取） */
    private Integer elderId;

    /** 开始时间（可选，格式 yyyy-MM-dd'T'HH:mm:ss） */
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;

    /** 结束时间（可选，格式 yyyy-MM-dd'T'HH:mm:ss） */
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate;

    /** 页码（默认 1，传 0 不分页） */
    private Integer pageNum = 1;

    /** 每页大小（默认 20，传 0 不分页） */
    private Integer pageSize = 20;
}
