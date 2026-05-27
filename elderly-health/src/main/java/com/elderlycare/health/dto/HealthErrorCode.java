package com.elderlycare.health.dto;

import com.elderlycare.common.core.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 健康服务错误码 (4000-4999)
 *
 * @author 郑
 */
@Getter
@AllArgsConstructor
public enum HealthErrorCode implements ErrorCode {

    /**
     * 健康记录不存在
     */
    HEALTH_RECORD_NOT_EXIST(4001, "健康记录不存在");

    private final Integer code;
    private final String message;
}
