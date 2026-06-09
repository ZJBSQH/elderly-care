package com.elderlycare.news.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.news.entity.NewsCollect;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 文章收藏记录 Mapper
 *
 * @author 郑
 */
@Mapper
public interface NewsCollectMapper extends BaseMapper<NewsCollect> {

    /**
     * 根据用户ID查询收藏记录列表
     *
     * @param userId 用户ID
     * @return 收藏记录列表
     */
    default List<NewsCollect> selectByUserId(Integer userId) {
        LambdaQueryWrapper<NewsCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NewsCollect::getUserId, userId)
                .orderByDesc(NewsCollect::getCreateTime);
        return selectList(wrapper);
    }

    /**
     * 检查用户是否已对某篇文章收藏
     */
    default boolean existsByUserAndNews(Integer userId, Integer newsId) {
        LambdaQueryWrapper<NewsCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NewsCollect::getUserId, userId)
                .eq(NewsCollect::getNewsId, newsId);
        return selectCount(wrapper) > 0;
    }

    /**
     * 批量查询用户收藏的文章ID集合（避免N+1）
     */
    default Set<Integer> selectCollectedNewsIds(Integer userId, List<Integer> newsIds) {
        if (newsIds == null || newsIds.isEmpty()) {
            return Collections.emptySet();
        }
        LambdaQueryWrapper<NewsCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NewsCollect::getUserId, userId)
                .in(NewsCollect::getNewsId, newsIds);
        return selectList(wrapper).stream()
                .map(NewsCollect::getNewsId)
                .collect(Collectors.toSet());
    }
}
