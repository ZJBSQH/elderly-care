package com.elderlycare.ai.dto;

import com.elderlycare.common.core.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI服务错误码 (7000-7999)
 *
 * @author 郑
 */
@Getter
@AllArgsConstructor
public enum AiErrorCode implements ErrorCode {

    AI_SERVICE_ERROR(7001, "AI服务异常"),
    AI_REMINDER_FAILED(7002, "获取用药提醒失败"),
    AI_MISSED_INTERVENTION_FAILED(7003, "获取漏服干预数据失败");

    private final Integer code;
    private final String message;
}
