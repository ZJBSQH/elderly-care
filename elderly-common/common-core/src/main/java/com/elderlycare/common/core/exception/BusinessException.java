package com.elderlycare.common.core.exception;

import lombok.Getter;

/**
 * 业务异常，携带错误码
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    /** 使用默认错误码 500 创建业务异常 */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    /** 使用指定的错误码和消息创建业务异常 */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /** 使用错误码枚举创建业务异常 */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    /** 使用错误码枚举和自定义消息创建业务异常 */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
}
