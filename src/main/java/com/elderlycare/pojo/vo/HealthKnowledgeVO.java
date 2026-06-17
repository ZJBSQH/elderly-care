package com.elderlycare.pojo.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 健康科普文章视图对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthKnowledgeVO {

    /**
     * 文章 ID
     */
    private Integer id;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 摘要
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
     * 阅读量
     */
    private Integer viewCount;

    /**
     * 是否推荐（0-否，1-是）
     */
    private Integer isRecommended;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 收藏数
     */
    private Integer collectCount;

    /**
     * 是否已点赞（0-否，1-是）
     */
    private Integer isLiked = 0;

    /**
     * 是否已收藏（0-否，1-是）
     */
    private Integer isCollected = 0;
}
