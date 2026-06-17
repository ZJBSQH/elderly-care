package com.elderlycare.common.exception;

import com.elderlycare.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常（我们自己抛的）
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> businessExceptionHandler(BusinessException e) {
        log.error("[业务异常]：{}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常（@Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        log.error("[参数校验异常]：{}", msg);
        return Result.error(ErrorCode.PARAM_ERROR.getCode(), msg);
    }

    /**
     * 参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<?> bindExceptionHandler(BindException e) {
        FieldError fieldError = e.getFieldError();
        String msg = fieldError != null ? fieldError.getDefaultMessage() : "参数绑定失败";
        log.error("[参数绑定异常]：{}", msg);
        return Result.error(ErrorCode.PARAM_ERROR.getCode(), msg);
    }

    /**
     * 非法参数
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> illegalArgumentExceptionHandler(IllegalArgumentException e) {
        log.error("[非法参数]：{}", e.getMessage());
        return Result.error(ErrorCode.PARAM_ERROR.getCode(), e.getMessage());
    }

    /**
     * 空指针
     */
    @ExceptionHandler(NullPointerException.class)
    public Result<?> nullPointerExceptionHandler(NullPointerException e) {
        log.error("[空指针异常]", e);
        return Result.error(ErrorCode.SYSTEM_ERROR.getCode(), "空指针异常");
    }

    /**
     * 运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("[运行时异常]", e);
        return Result.error(ErrorCode.SYSTEM_ERROR.getCode(), "系统异常：" + e.getMessage());
    }

    /**
     * 最大范围：所有异常兜底
     */
    @ExceptionHandler(Exception.class)
    public Result<?> exceptionHandler(Exception e) {
        log.error("[系统异常]", e);
        return Result.error(ErrorCode.SYSTEM_ERROR.getCode(), "服务器繁忙，请稍后重试");
    }
}