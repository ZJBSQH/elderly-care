package com.elderlycare.news.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 健康知识文章展示对象
 *
 * @author 郑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthKnowledgeVO {

    /** 文章ID */
    private Integer id;

    /** 文章标题 */
    private String title;

    /** 文章摘要 */
    private String summary;

    /** 文章分类 */
    private String category;

    /** 封面图片地址 */
    private String coverImage;

    /** 浏览次数 */
    private Integer viewCount;

    /** 是否推荐 */
    private Integer isRecommended;

    /** 发布时间 */
    private LocalDateTime publishTime;

    /** 点赞次数 */
    private Integer likeCount;

    /** 收藏次数 */
    private Integer collectCount;

    /** 当前用户是否已点赞 */
    private Boolean isLiked;

    /** 当前用户是否已收藏 */
    private Boolean isCollected;
}
