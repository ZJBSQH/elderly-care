package com.elderlycare.medicine.dto;

import com.elderlycare.common.core.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用药服务错误码
 *
 * @author 郑
 */
@Getter
@AllArgsConstructor
public enum MedicineErrorCode implements ErrorCode {

    /**
     * 用药计划不存在
     */
    MEDICINE_NOT_EXIST(5001, "用药计划不存在");

    /** 错误码 */
    private final Integer code;
    /** 错误信息 */
    private final String message;
}
