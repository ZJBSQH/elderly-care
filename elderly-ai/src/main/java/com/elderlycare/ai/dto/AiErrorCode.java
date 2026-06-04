package com.elderlycare.ai.dto;

import com.elderlycare.common.core.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI服务错误码枚举
 * <p>
 * 实现 {@link ErrorCode} 接口，错误码范围 7000-7999，
 * 用于 AI 服务中异常场景的统一错误标识。
 *
 * @author 郑
 */
@Getter
@AllArgsConstructor
public enum AiErrorCode implements ErrorCode {

    /** AI服务内部异常，如模型调用失败、数据处理错误等 */
    AI_SERVICE_ERROR(7001, "AI服务异常"),

    /** 调用用药提醒服务失败，无法获取今日提醒数据 */
    AI_REMINDER_FAILED(7002, "获取用药提醒失败"),

    /** 获取漏服干预数据失败，无法判断漏服情况并生成干预建议 */
    AI_MISSED_INTERVENTION_FAILED(7003, "获取漏服干预数据失败");

    /** 错误码 */
    private final Integer code;

    /** 错误信息 */
    private final String message;
}
