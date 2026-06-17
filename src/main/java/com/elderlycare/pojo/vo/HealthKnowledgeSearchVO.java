package com.elderlycare.pojo.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 搜索结果视图对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthKnowledgeSearchVO {

    /**
     * 文章列表
     */
    private List<HealthKnowledgeVO> articles;

    /**
     * 总数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer page;

    /**
     * 每页数量
     */
    private Integer size;

    /**
     * 总页数
     */
    private Integer totalPages;
}
