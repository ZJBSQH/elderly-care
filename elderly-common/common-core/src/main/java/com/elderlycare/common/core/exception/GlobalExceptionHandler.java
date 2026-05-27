package com.elderlycare.common.core.exception;

import com.elderlycare.common.core.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> businessExceptionHandler(BusinessException e) {
        log.error("[业务异常]：{}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        log.error("[参数校验异常]：{}", msg);
        return Result.error(BaseErrorCode.PARAM_ERROR.getCode(), msg);
    }

    @ExceptionHandler(BindException.class)
    public Result<?> bindExceptionHandler(BindException e) {
        FieldError fieldError = e.getFieldError();
        String msg = fieldError != null ? fieldError.getDefaultMessage() : "参数绑定失败";
        log.error("[参数绑定异常]：{}", msg);
        return Result.error(BaseErrorCode.PARAM_ERROR.getCode(), msg);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> illegalArgumentExceptionHandler(IllegalArgumentException e) {
        log.error("[非法参数]：{}", e.getMessage());
        return Result.error(BaseErrorCode.PARAM_ERROR.getCode(), e.getMessage());
    }

    @ExceptionHandler(NullPointerException.class)
    public Result<?> nullPointerExceptionHandler(NullPointerException e) {
        log.error("[空指针异常]", e);
        return Result.error(BaseErrorCode.SYSTEM_ERROR.getCode(), "空指针异常");
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("[运行时异常]", e);
        return Result.error(BaseErrorCode.SYSTEM_ERROR.getCode(), "系统异常：" + e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<?> exceptionHandler(Exception e) {
        log.error("[系统异常]", e);
        return Result.error(BaseErrorCode.SYSTEM_ERROR.getCode(), "服务器繁忙，请稍后重试");
    }
}
