package com.elderlycare.common.core.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.function.Function;

/**
 * 统一响应包装
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    /** 获取响应数据 */
    public T getData() {
        return data;
    }

    /** 返回成功响应（无数据） */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    /** 返回成功响应（带数据） */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /** 返回成功响应（带数据和自定义消息） */
    public static <T> Result<T> success(T data, String message) {
        Result<T> result = new Result<>();
        result.code = 200;
        result.data = data;
        result.message = message;
        return result;
    }

    /** 返回错误响应（默认错误码 500） */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    /** 返回错误响应（指定错误码和消息） */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /** 对响应数据进行类型转换（保持 code 和 message 不变） */
    public <R> Result<R> map(Function<T, R> mapper) {
        Result<R> result = new Result<>();
        result.code = this.code;
        result.message = this.message;
        result.data = this.data != null ? mapper.apply(this.data) : null;
        return result;
    }


}
