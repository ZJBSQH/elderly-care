package com.elderlycare.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 请求基类
 * 所有 DTO 的父类，提供通用功能
 */
@Data
public abstract class BaseRequest implements Serializable {
    private static final long serialVersionUID = 1L;
}
