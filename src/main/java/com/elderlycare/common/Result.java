package com.elderlycare.common;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private Integer code;
    private String message;
    private T data;


    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> success(T data, String message) {
        Result<T> result = new Result<>();
        result.code = 200;
        result.data = data;
        result.message = message;
        return result;
    }

    /**
     * 转换 Result 中的数据
     */
    public <R> Result<R> map(java.util.function.Function<T, R> mapper) {
        Result<R> result = new Result<>();
        result.code = this.code;
        result.message = this.message;
        result.data = this.data != null ? mapper.apply(this.data) : null;
        return result;
    }

    }
