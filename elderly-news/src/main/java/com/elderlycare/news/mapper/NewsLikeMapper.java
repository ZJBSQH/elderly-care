package com.elderlycare.news.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.news.entity.NewsLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章点赞记录 Mapper
 *
 * @author 郑
 */
@Mapper
public interface NewsLikeMapper extends BaseMapper<NewsLike> {

    /**
     * 检查用户是否已对某篇文章点赞
     *
     * @param userId 用户ID
     * @param newsId 文章ID
     * @return 是否存在点赞记录
     */
    default boolean existsByUserAndNews(Integer userId, Integer newsId) {
        LambdaQueryWrapper<NewsLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NewsLike::getUserId, userId)
                .eq(NewsLike::getNewsId, newsId);
        return selectCount(wrapper) > 0;
    }
}
