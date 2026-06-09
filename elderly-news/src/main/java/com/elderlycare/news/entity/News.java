package com.elderlycare.news.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 健康资讯文章实体
 *
 * @author 郑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("news")
public class News {

    /** 文章ID */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 文章标题 */
    private String title;

    /** 文章内容 */
    private String content;

    /** 文章摘要 */
    private String summary;

    /** 封面图片地址 */
    private String coverImage;

    /** 文章分类 */
    private String category;

    /** 浏览次数 */
    private Integer viewCount;

    /** 点赞次数 */
    private Integer likeCount;

    /** 收藏次数 */
    private Integer collectCount;

    /** 是否推荐 (0-不推荐, 1-推荐) */
    private Integer isRecommended;

    /** 创建者ID */
    private Integer creatorId;

    /** 发布时间 */
    private LocalDateTime publishTime;

    /** 状态 (0-下架, 1-上架) */
    private Integer status;
}
