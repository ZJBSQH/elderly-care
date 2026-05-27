package com.elderlycare.common.core.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 基础错误码（公共模块共享）
 */
@Getter
@AllArgsConstructor
public enum BaseErrorCode implements ErrorCode {

    SUCCESS(200, "操作成功"),

    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权访问"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),

    SYSTEM_ERROR(500, "系统错误"),
    DATABASE_ERROR(9001, "数据库操作失败"),
    REDIS_ERROR(9002, "缓存操作失败");

    private final Integer code;
    private final String message;
}
