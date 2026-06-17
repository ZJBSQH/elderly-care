package com.elderlycare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderlycare.pojo.entity.NewsCollect;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 健康科普收藏 Mapper（使用 NewsCollect 实体）
 */
@Mapper
public interface HealthKnowledgeCollectMapper extends BaseMapper<NewsCollect> {

    /**
     * 查询用户的所有收藏
     */
    default List<NewsCollect> selectByUserId(@Param("userId") Integer userId) {
        LambdaQueryWrapper<NewsCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NewsCollect::getUserId, userId)
                .orderByDesc(NewsCollect::getCreateTime);
        return selectList(wrapper);
    }

    /**
     * 检查是否已收藏
     */
    default boolean exists(@Param("userId") Integer userId, @Param("newsId") Integer newsId) {
        LambdaQueryWrapper<NewsCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NewsCollect::getUserId, userId)
               .eq(NewsCollect::getNewsId, newsId);
        return selectCount(wrapper) > 0;
    }

    /**
     * 删除收藏
     */
    default int deleteByUserAndNews(@Param("userId") Integer userId, @Param("newsId") Integer newsId) {
        LambdaQueryWrapper<NewsCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NewsCollect::getUserId, userId)
               .eq(NewsCollect::getNewsId, newsId);
        return delete(wrapper);
    }
}
