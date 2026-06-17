package com.elderlycare.pojo.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 视图对象基类
 * 所有 VO 的父类，提供通用功能
 */
@Data
public abstract class BaseVO implements Serializable {
    private static final long serialVersionUID = 1L;
}
