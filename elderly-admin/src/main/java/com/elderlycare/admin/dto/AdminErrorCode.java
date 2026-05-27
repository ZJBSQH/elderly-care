package com.elderlycare.admin.dto;

import com.elderlycare.common.core.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 后台管理服务错误码 (9000-9999)
 *
 * @author 郑
 */
@Getter
@AllArgsConstructor
public enum AdminErrorCode implements ErrorCode {

    /**
     * 系统配置不存在
     */
    CONFIG_NOT_FOUND(9001, "系统配置不存在"),

    /**
     * 疾病不存在
     */
    DISEASE_NOT_FOUND(9002, "疾病不存在");

    private final Integer code;
    private final String message;
}
