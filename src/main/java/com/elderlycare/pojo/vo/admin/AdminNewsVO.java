package com.elderlycare.pojo.vo.admin;

import java.time.LocalDateTime;

/**
 * 资讯管理 VO
 */
public record AdminNewsVO(
    /**
     * 资讯 ID
     */
    Integer id,

    /**
     * 标题
     */
    String title,

    /**
     * 摘要
     */
    String summary,

    /**
     * 分类
     */
    String category,

    /**
     * 封面图片 URL
     */
    String coverImage,

    /**
     * 阅读量
     */
    Integer viewCount,

    /**
     * 是否推荐
     */
    Integer isRecommended,

    /**
     * 推荐状态描述
     */
    String recommendedDesc,

    /**
     * 状态：0-草稿，1-已发布，2-下架
     */
    Integer status,

    /**
     * 状态描述
     */
    String statusDesc,

    /**
     * 创建者 ID
     */
    Integer creatorId,

    /**
     * 发布时间
     */
    LocalDateTime publishTime
) {}
