package com.elderlycare.pojo.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 收藏列表视图对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthKnowledgeCollectVO {

    /**
     * 收藏 ID
     */
    private Integer id;

    /**
     * 文章 ID
     */
    private Integer newsId;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章摘要
     */
    private String summary;

    /**
     * 分类
     */
    private String category;

    /**
     * 封面图片 URL
     */
    private String coverImage;

    /**
     * 收藏时间
     */
    private LocalDateTime collectTime;
}
