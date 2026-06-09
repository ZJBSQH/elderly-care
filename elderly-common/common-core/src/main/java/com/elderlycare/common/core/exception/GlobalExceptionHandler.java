package com.elderlycare.common.core.exception;

import com.elderlycare.common.core.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 处理业务异常 */
    @ExceptionHandler(BusinessException.class)
    public Result<?> businessExceptionHandler(BusinessException e) {
        log.error("[业务异常]：{}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /** 处理方法参数校验异常（@Valid 校验失败） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        log.error("[参数校验异常]：{}", msg);
        return Result.error(BaseErrorCode.PARAM_ERROR.getCode(), msg);
    }

    /** 处理参数绑定异常 */
    @ExceptionHandler(BindException.class)
    public Result<?> bindExceptionHandler(BindException e) {
        FieldError fieldError = e.getFieldError();
        String msg = fieldError != null ? fieldError.getDefaultMessage() : "参数绑定失败";
        log.error("[参数绑定异常]：{}", msg);
        return Result.error(BaseErrorCode.PARAM_ERROR.getCode(), msg);
    }

    /** 处理缺少请求参数异常 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<?> missingServletRequestParameterExceptionHandler(MissingServletRequestParameterException e) {
        log.error("[缺少请求参数]：{}", e.getParameterName());
        return Result.error(BaseErrorCode.PARAM_ERROR.getCode(), "缺少必填参数: " + e.getParameterName());
    }

    /** 处理非法参数异常 */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> illegalArgumentExceptionHandler(IllegalArgumentException e) {
        log.error("[非法参数]：{}", e.getMessage());
        return Result.error(BaseErrorCode.PARAM_ERROR.getCode(), e.getMessage());
    }

    /** 处理运行时异常 */
    @ExceptionHandler(RuntimeException.class)
    public Result<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("[运行时异常]", e);
        return Result.error(BaseErrorCode.SYSTEM_ERROR.getCode(), "系统异常，请稍后重试");
    }

    /** 处理所有未捕获的系统异常（兜底） */
    @ExceptionHandler(Exception.class)
    public Result<?> exceptionHandler(Exception e) {
        log.error("[系统异常]", e);
        return Result.error(BaseErrorCode.SYSTEM_ERROR.getCode(), "服务器繁忙，请稍后重试");
    }
}
