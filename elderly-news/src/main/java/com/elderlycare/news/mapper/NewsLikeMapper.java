package com.elderlycare.news.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.news.entity.NewsLike;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 文章点赞记录 Mapper
 *
 * @author 郑
 */
@Mapper
public interface NewsLikeMapper extends BaseMapper<NewsLike> {

    /**
     * 检查用户是否已对某篇文章点赞
     */
    default boolean existsByUserAndNews(Integer userId, Integer newsId) {
        LambdaQueryWrapper<NewsLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NewsLike::getUserId, userId)
                .eq(NewsLike::getNewsId, newsId);
        return selectCount(wrapper) > 0;
    }

    /**
     * 批量查询用户点赞的文章ID集合（避免N+1）
     */
    default Set<Integer> selectLikedNewsIds(Integer userId, List<Integer> newsIds) {
        if (newsIds == null || newsIds.isEmpty()) {
            return Collections.emptySet();
        }
        LambdaQueryWrapper<NewsLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NewsLike::getUserId, userId)
                .in(NewsLike::getNewsId, newsIds);
        return selectList(wrapper).stream()
                .map(NewsLike::getNewsId)
                .collect(Collectors.toSet());
    }
}
