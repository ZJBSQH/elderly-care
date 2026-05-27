package com.elderlycare.common.core.exception;

/**
 * 错误码接口，各服务实现此接口定义自己的错误码枚举
 */
public interface ErrorCode {
    Integer getCode();
    String getMessage();
}
