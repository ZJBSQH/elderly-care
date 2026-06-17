package com.elderlycare.pojo.vo;

import java.util.List;

/**
 * 通用分页结果 VO
 */
public record PageResult<T>(
    /**
     * 当前页数据列表
     */
    List<T> list,

    /**
     * 总记录数
     */
    Long total,

    /**
     * 当前页码（从 1 开始）
     */
    Integer page,

    /**
     * 每页条数
     */
    Integer size
) {
    /**
     * 静态工厂方法
     */
    public static <T> PageResult<T> of(List<T> list, Long total, Integer page, Integer size) {
        return new PageResult<>(list, total, page, size);
    }
}
