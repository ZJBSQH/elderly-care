package com.elderlycare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.mapper.NewsLikeMapper;
import com.elderlycare.mapper.NewsMapper;
import com.elderlycare.mapper.HealthKnowledgeCollectMapper;
import com.elderlycare.common.Result;
import com.elderlycare.common.exception.BusinessException;
import com.elderlycare.common.exception.ErrorCode;
import com.elderlycare.common.util.BeanUtil;
import org.springframework.beans.BeanUtils;
import com.elderlycare.pojo.dto.news.CollectRequest;
import com.elderlycare.pojo.dto.news.LikeRequest;
import com.elderlycare.pojo.dto.news.NewsAddRequest;
import com.elderlycare.pojo.dto.news.NewsUpdateRequest;
import com.elderlycare.pojo.entity.News;
import com.elderlycare.pojo.entity.NewsCollect;
import com.elderlycare.pojo.entity.NewsLike;
import com.elderlycare.pojo.vo.HealthKnowledgeCollectVO;
import com.elderlycare.pojo.vo.HealthKnowledgeSearchVO;
import com.elderlycare.pojo.vo.HealthKnowledgeVO;
import com.elderlycare.service.NewsService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * 健康科普服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private final NewsMapper newsMapper;
    private final NewsLikeMapper newsLikeMapper;
    private final HealthKnowledgeCollectMapper healthKnowledgeCollectMapper;

    // 缓存 key
    private static final String CACHE_RECOMMENDED = "NEW_RECOMMENDED";
    private static final long CACHE_TTL = 30;

    private final ObjectMapper objectMapper;
    private final ReentrantLock recommendedLock = new ReentrantLock();

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    /**
     * 发布健康科普文章
     * <p>
     * 该方法用于创建并发布新的健康科普文章。发布时会自动初始化文章的统计数据
     * （浏览量、点赞数、收藏数），并记录发布时间和创建者信息。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> publishArticle(NewsAddRequest request, Integer creatorId) {
        News news = new News();
        BeanUtils.copyProperties(request, news);
        news.setViewCount(0);
        news.setLikeCount(0);
        news.setCollectCount(0);
        news.setCreatorId(creatorId);
        news.setPublishTime(LocalDateTime.now());

        newsMapper.insert(news);
        log.info("发布健康科普文章成功，id: {}, title: {}", news.getId(), news.getTitle());

        return Result.success(news.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updateArticle(Integer id, NewsUpdateRequest request) {
        if (id == null) {
            throw new BusinessException("文章 ID 不能为空");
        }

        News news = newsMapper.selectById(id);
        if (news == null) {
            throw new BusinessException("文章不存在");
        }

        BeanUtil.copyNonNullProperties(request, news);

        newsMapper.updateById(news);

        // 删除缓存
        stringRedisTemplate.delete(CACHE_RECOMMENDED);
        log.info("更新健康科普文章成功，id: {}", id);

        return Result.success(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteArticle(Integer id) {
        if (id == null) {
            throw new BusinessException("文章 ID 不能为空");
        }

        int rows = newsMapper.deleteById(id);
        if (rows > 0) {
            stringRedisTemplate.delete(CACHE_RECOMMENDED);
            log.info("删除健康科普文章成功，id: {}", id);
            return Result.success(null);
        } else {
            throw new BusinessException("删除失败，文章不存在");
        }
    }

    @Override
    public Result<HealthKnowledgeVO> getArticleDetail(Integer id, Integer userId) {
        if (id == null) {
            throw new BusinessException("文章 ID 不能为空");
        }

        // 增加阅读量
        newsMapper.incrementViewCount(id);

        News news = newsMapper.selectById(id);
        if (news == null) {
            throw new BusinessException("文章不存在");
        }

        HealthKnowledgeVO vo = convertToVO(news);

        // 检查是否已收藏和已点赞
        if (userId != null) {
            boolean collected = healthKnowledgeCollectMapper.exists(userId, id);
            vo.setIsCollected(collected ? 1 : 0);
            boolean liked = newsLikeMapper.exists(userId, id);
            vo.setIsLiked(liked ? 1 : 0);
        }

        return Result.success(vo);
    }

    @Override
    public Result<List<HealthKnowledgeVO>> getArticlesByCategory(String category, Integer userId) {
        List<News> articles;

        if (category != null && !category.isEmpty()) {
            articles = newsMapper.selectByCategory(category);
        } else {
            articles = newsMapper.selectList(null);
        }

        List<HealthKnowledgeVO> voList = articles.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 批量设置收藏和点赞状态
        if (userId != null) {
            setCollectionStatus(voList, userId);
            setLikeStatus(voList, userId);
        }

        return Result.success(voList);
    }

    @Override
    public Result<List<HealthKnowledgeVO>> getRecommendedArticles(Integer userId) {
        // 1. 查缓存
        String cached = stringRedisTemplate.opsForValue().get(CACHE_RECOMMENDED);
        if (cached != null) {
            List<HealthKnowledgeVO> voList = deserializeVOList(cached);
            fillUserStatus(voList, userId);
            return Result.success(voList);
        }

        // 2. 缓存未命中 → 加锁防击穿
        recommendedLock.lock();
        try {
            // 双重检查
            cached = stringRedisTemplate.opsForValue().get(CACHE_RECOMMENDED);
            if (cached != null) {
                List<HealthKnowledgeVO> voList = deserializeVOList(cached);
                fillUserStatus(voList, userId);
                return Result.success(voList);
            }

            // 3. 查 DB 并写缓存
            List<News> articles = newsMapper.selectRecommended();
            List<HealthKnowledgeVO> voList = articles.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());

            String json = objectMapper.writeValueAsString(voList);
            stringRedisTemplate.opsForValue().set(CACHE_RECOMMENDED, json, CACHE_TTL, TimeUnit.MINUTES);
            log.info("精选文章缓存已更新，共 {} 篇", voList.size());

            fillUserStatus(voList, userId);
            return Result.success(voList);
        } catch (Exception e) {
            log.error("获取精选文章缓存异常", e);
            // 降级：直接查 DB
            List<News> articles = newsMapper.selectRecommended();
            List<HealthKnowledgeVO> voList = articles.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            fillUserStatus(voList, userId);
            return Result.success(voList);
        } finally {
            recommendedLock.unlock();
        }
    }

    /**
     * 反序列化缓存的 JSON 为 VO 列表
     */
    private List<HealthKnowledgeVO> deserializeVOList(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<HealthKnowledgeVO>>() {});
        } catch (Exception e) {
            log.error("反序列化精选文章缓存失败", e);
            return null;
        }
    }

    /**
     * 填充用户收藏和点赞状态
     */
    private void fillUserStatus(List<HealthKnowledgeVO> voList, Integer userId) {
        if (userId != null && voList != null) {
            setCollectionStatus(voList, userId);
            setLikeStatus(voList, userId);
        }
    }

    @Override
    public Result<List<HealthKnowledgeVO>> getPopularArticles(Integer limit, Integer userId) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        List<News> articles = newsMapper.selectPopular(limit);

        List<HealthKnowledgeVO> voList = articles.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        if (userId != null) {
            setCollectionStatus(voList, userId);
            setLikeStatus(voList, userId);
        }

        return Result.success(voList);
    }

    @Override
    public Result<HealthKnowledgeSearchVO> searchArticles(String keyword, String category, Integer page, Integer size, Integer userId) {
        if (page == null || page <= 0) {
            page = 1;
        }
        if (size == null || size <= 0) {
            size = 10;
        }

        Page<News> mpPage = new Page<>(page, size);

        // 构建查询条件
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<News>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(News::getTitle, keyword)
                    .or()
                    .like(News::getContent, keyword)
                    .or()
                    .like(News::getSummary, keyword));
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(News::getCategory, category);
        }
        wrapper.orderByDesc(News::getPublishTime);

        Page<News> resultPage = newsMapper.selectPage(mpPage, wrapper);

        List<HealthKnowledgeVO> voList = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 设置收藏和点赞状态
        if (userId != null) {
            setCollectionStatus(voList, userId);
            setLikeStatus(voList, userId);
        }

        HealthKnowledgeSearchVO searchVO = new HealthKnowledgeSearchVO();
        searchVO.setArticles(voList);
        searchVO.setTotal(resultPage.getTotal());
        searchVO.setPage(page);
        searchVO.setSize(size);
        searchVO.setTotalPages((int) ((resultPage.getTotal() + size - 1) / size));

        return Result.success(searchVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> likeArticle(LikeRequest request, Integer userId) {
        if (request.getNewsId() == null) {
            throw new BusinessException("资讯 ID 不能为空");
        }

        // 检查是否已点赞
        if (newsLikeMapper.exists(userId, request.getNewsId())) {
            throw new BusinessException(ErrorCode.ALREADY_LIKED);
        }

        // 检查资讯是否存在
        News news = newsMapper.selectById(request.getNewsId());
        if (news == null) {
            throw new BusinessException("资讯不存在");
        }

        // 插入点赞记录
        NewsLike like = new NewsLike();
        like.setUserId(userId);
        like.setNewsId(request.getNewsId());
        like.setCreateTime(LocalDateTime.now());
        newsLikeMapper.insert(like);

        // 原子更新点赞数
        newsMapper.incrementLikeCount(request.getNewsId());

        // 重新读取最新计数，检查是否达到精选条件
        news = newsMapper.selectById(request.getNewsId());
        checkAndMarkFeatured(news);

        log.info("点赞成功，userId: {}, newsId: {}", userId, request.getNewsId());
        return Result.success(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> cancelLikeArticle(LikeRequest request, Integer userId) {
        if (request.getNewsId() == null) {
            throw new BusinessException("资讯 ID 不能为空");
        }

        int rows = newsLikeMapper.deleteByUserAndNews(userId, request.getNewsId());
        if (rows > 0) {
            // 原子减少点赞数
            newsMapper.decrementLikeCount(request.getNewsId());

            log.info("取消点赞成功，userId: {}, newsId: {}", userId, request.getNewsId());
            return Result.success(null);
        } else {
            throw new BusinessException(ErrorCode.LIKE_NOT_EXIST);
        }
    }

    @Override
    public Result<Boolean> checkIfLiked(Integer newsId, Integer userId) {
        if (newsId == null || userId == null) {
            throw new BusinessException("参数不能为空");
        }
        boolean liked = newsLikeMapper.exists(userId, newsId);
        return Result.success(liked);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> collectArticle(CollectRequest request, Integer userId) {
        if (request.getNewsId() == null) {
            throw new BusinessException("文章 ID 不能为空");
        }

        // 检查是否已收藏
        if (healthKnowledgeCollectMapper.exists(userId, request.getNewsId())) {
            throw new BusinessException("已收藏该文章");
        }

        // 检查文章是否存在
        News news = newsMapper.selectById(request.getNewsId());
        if (news == null) {
            throw new BusinessException("文章不存在");
        }

        NewsCollect collect = new NewsCollect();
        collect.setUserId(userId);
        collect.setNewsId(request.getNewsId());
        collect.setCreateTime(LocalDateTime.now());

        healthKnowledgeCollectMapper.insert(collect);

        // 原子更新收藏数
        newsMapper.incrementCollectCount(request.getNewsId());

        // 重新读取最新计数，检查是否达到精选条件
        news = newsMapper.selectById(request.getNewsId());
        checkAndMarkFeatured(news);

        log.info("收藏文章成功，userId: {}, newsId: {}", userId, request.getNewsId());

        return Result.success(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> cancelCollectArticle(CollectRequest request, Integer userId) {
        if (request.getNewsId() == null) {
            throw new BusinessException("文章 ID 不能为空");
        }

        int rows = healthKnowledgeCollectMapper.deleteByUserAndNews(userId, request.getNewsId());
        if (rows > 0) {
            // 原子减少收藏数
            newsMapper.decrementCollectCount(request.getNewsId());

            log.info("取消收藏成功，userId: {}, newsId: {}", userId, request.getNewsId());
            return Result.success(null);
        } else {
            throw new BusinessException("未找到该收藏记录");
        }
    }

    @Override
    public Result<List<HealthKnowledgeCollectVO>> getUserCollects(Integer userId) {
        List<NewsCollect> collects = healthKnowledgeCollectMapper.selectByUserId(userId);

        if (collects.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        // 批量查询资讯，避免 N+1
        List<Integer> newsIds = collects.stream().map(NewsCollect::getNewsId).distinct().toList();
        Map<Integer, News> newsMap = newsMapper.selectBatchIds(newsIds).stream()
                .collect(Collectors.toMap(News::getId, n -> n));

        List<HealthKnowledgeCollectVO> voList = new ArrayList<>();
        for (NewsCollect collect : collects) {
            News news = newsMap.get(collect.getNewsId());
            if (news != null) {
                HealthKnowledgeCollectVO vo = new HealthKnowledgeCollectVO();
                vo.setId(collect.getId());
                vo.setNewsId(collect.getNewsId());
                vo.setTitle(news.getTitle());
                vo.setSummary(news.getSummary());
                vo.setCategory(news.getCategory());
                vo.setCoverImage(news.getCoverImage());
                vo.setCollectTime(collect.getCreateTime());
                voList.add(vo);
            }
        }

        return Result.success(voList);
    }

    @Override
    public Result<Boolean> checkIfCollected(Integer newsId, Integer userId) {
        if (newsId == null || userId == null) {
            throw new BusinessException("参数不能为空");
        }

        boolean collected = healthKnowledgeCollectMapper.exists(userId, newsId);
        return Result.success(collected);
    }

    /**
     * 转换为 VO
     */
    private HealthKnowledgeVO convertToVO(News news) {
        HealthKnowledgeVO vo = new HealthKnowledgeVO();
        BeanUtils.copyProperties(news, vo);
        vo.setLikeCount(news.getLikeCount() != null ? news.getLikeCount() : 0);
        vo.setCollectCount(news.getCollectCount() != null ? news.getCollectCount() : 0);
        return vo;
    }

    /**
     * 批量设置收藏状态
     */
    /**
     * 批量设置收藏状态（一次查询，避免 N+1）
     */
    private void setCollectionStatus(List<HealthKnowledgeVO> voList, Integer userId) {
        if (voList.isEmpty()) return;
        List<Integer> newsIds = voList.stream().map(HealthKnowledgeVO::getId).toList();
        Set<Integer> collectedIds = healthKnowledgeCollectMapper.selectList(
                new LambdaQueryWrapper<NewsCollect>()
                        .eq(NewsCollect::getUserId, userId)
                        .in(NewsCollect::getNewsId, newsIds)
        ).stream().map(NewsCollect::getNewsId).collect(Collectors.toSet());
        voList.forEach(vo -> vo.setIsCollected(collectedIds.contains(vo.getId()) ? 1 : 0));
    }

    /**
     * 批量设置点赞状态（一次查询，避免 N+1）
     */
    private void setLikeStatus(List<HealthKnowledgeVO> voList, Integer userId) {
        if (voList.isEmpty()) return;
        List<Integer> newsIds = voList.stream().map(HealthKnowledgeVO::getId).toList();
        Set<Integer> likedIds = newsLikeMapper.selectList(
                new LambdaQueryWrapper<NewsLike>()
                        .eq(NewsLike::getUserId, userId)
                        .in(NewsLike::getNewsId, newsIds)
        ).stream().map(NewsLike::getNewsId).collect(Collectors.toSet());
        voList.forEach(vo -> vo.setIsLiked(likedIds.contains(vo.getId()) ? 1 : 0));
    }

    /**
     * 检查并自动标记精选：点赞数 ≥ 50 且 收藏数 ≥ 10 时自动设为精选
     */
    private void checkAndMarkFeatured(News news) {
        int likeCount = news.getLikeCount() != null ? news.getLikeCount() : 0;
        int collectCount = news.getCollectCount() != null ? news.getCollectCount() : 0;

        if (likeCount >= 50 && collectCount >= 10 && (news.getIsRecommended() == null || news.getIsRecommended() != 1)) {
            news.setIsRecommended(1);
            newsMapper.updateById(news);
            log.info("资讯 {} 自动标记为精选（点赞数: {}, 收藏数: {}）", news.getId(), likeCount, collectCount);
            // 清除 Redis 中的推荐文章缓存
            stringRedisTemplate.delete(CACHE_RECOMMENDED);
        }
    }
}
