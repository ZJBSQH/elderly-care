package com.elderlycare.news.service.impl;

import com.elderlycare.common.core.exception.BusinessException;
import com.elderlycare.common.core.result.Result;
import com.elderlycare.common.core.util.BeanUtil;
import com.elderlycare.common.security.util.SecurityUtil;
import com.elderlycare.news.dto.CollectRequest;
import com.elderlycare.news.dto.LikeRequest;
import com.elderlycare.news.dto.NewsAddRequest;
import com.elderlycare.news.dto.NewsUpdateRequest;
import com.elderlycare.news.entity.News;
import com.elderlycare.news.entity.NewsCollect;
import com.elderlycare.news.entity.NewsLike;
import com.elderlycare.news.mapper.NewsCollectMapper;
import com.elderlycare.news.mapper.NewsLikeMapper;
import com.elderlycare.news.mapper.NewsMapper;
import com.elderlycare.news.service.NewsService;
import com.elderlycare.news.vo.HealthKnowledgeVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.elderlycare.news.dto.NewsErrorCode.*;

/**
 * 资讯文章服务实现
 *
 * @author 郑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    /** 推荐文章Redis缓存键 */
    private static final String REDIS_RECOMMENDED_KEY = "news:recommended";
    /** 热门文章Redis缓存键 */
    private static final String REDIS_POPULAR_KEY = "news:popular";
    /** Redis缓存过期时间 */
    private static final Duration REDIS_CACHE_EXPIRE = Duration.ofMinutes(30);

    /** 资讯文章Mapper */
    private final NewsMapper newsMapper;
    /** 点赞记录Mapper */
    private final NewsLikeMapper newsLikeMapper;
    /** 收藏记录Mapper */
    private final NewsCollectMapper newsCollectMapper;
    /** Redis模板 */
    private final StringRedisTemplate redisTemplate;
    /** 安全工具 */
    private final SecurityUtil securityUtil;
    /** JSON序列化工具 */
    private final ObjectMapper objectMapper;

    /**
     * 发布文章
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> publishArticle(NewsAddRequest request) {
        Integer userId = securityUtil.getCurrentUserId();

        News news = new News();
        news.setTitle(request.getTitle());
        news.setContent(request.getContent());
        news.setSummary(request.getSummary());
        news.setCategory(request.getCategory());
        news.setCoverImage(request.getCoverImage());
        news.setViewCount(0);
        news.setLikeCount(0);
        news.setCollectCount(0);
        news.setIsRecommended(0);
        news.setCreatorId(userId);
        news.setPublishTime(LocalDateTime.now());
        news.setStatus(1);

        newsMapper.insert(news);
        log.info("用户{}发布文章成功，文章ID：{}", userId, news.getId());
        return Result.success();
    }

    /**
     * 更新文章
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updateArticle(NewsUpdateRequest request) {
        News news = newsMapper.selectById(request.getId());
        if (news == null) {
            throw new BusinessException(NEWS_NOT_EXIST);
        }

        BeanUtil.copyNonNullProperties(request, news);

        newsMapper.updateById(news);
        log.info("文章{}更新成功", request.getId());
        return Result.success();
    }

    /**
     * 删除文章
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteArticle(Integer id) {
        News news = newsMapper.selectById(id);
        if (news == null) {
            throw new BusinessException(NEWS_NOT_EXIST);
        }

        newsMapper.deleteById(id);
        log.info("文章{}删除成功", id);
        return Result.success();
    }

    /**
     * 获取文章详情（含点赞/收藏状态）
     */
    @Override
    public Result<Map<String, Object>> getArticleDetail(Integer id) {
        News news = newsMapper.selectById(id);
        if (news == null) {
            throw new BusinessException(NEWS_NOT_EXIST);
        }

        // 原子增加浏览次数
        newsMapper.incrementViewCount(id);
        news.setViewCount(news.getViewCount() + 1);

        Integer userId = securityUtil.getCurrentUserId();
        Map<String, Object> result = new HashMap<>();
        result.put("news", news);
        if (userId != null) {
            result.put("isLiked", newsLikeMapper.existsByUserAndNews(userId, id));
            result.put("isCollected", newsCollectMapper.existsByUserAndNews(userId, id));
        } else {
            result.put("isLiked", false);
            result.put("isCollected", false);
        }

        return Result.success(result);
    }

    /**
     * 根据分类查询文章列表
     */
    @Override
    public Result<List<HealthKnowledgeVO>> getArticlesByCategory(String category) {
        List<News> newsList = newsMapper.selectByCategory(category);
        List<HealthKnowledgeVO> voList = convertToVOList(newsList);
        return Result.success(voList);
    }

    /**
     * 获取推荐文章列表（优先从Redis缓存读取）
     */
    @Override
    public Result<List<HealthKnowledgeVO>> getRecommendedArticles() {
        // 先尝试从 Redis 缓存中获取
        try {
            String cached = redisTemplate.opsForValue().get(REDIS_RECOMMENDED_KEY);
            if (cached != null) {
                List<HealthKnowledgeVO> voList = objectMapper.readValue(cached, new TypeReference<List<HealthKnowledgeVO>>() {});
                log.info("从缓存中获取推荐文章列表，共{}条", voList.size());
                return Result.success(voList);
            }
        } catch (Exception e) {
            log.warn("从Redis读取推荐文章缓存失败，回退到数据库查询", e);
        }

        // 缓存未命中，从数据库查询
        List<News> newsList = newsMapper.selectRecommended();
        List<HealthKnowledgeVO> voList = convertToVOList(newsList);

        // 写入缓存
        try {
            String json = objectMapper.writeValueAsString(voList);
            redisTemplate.opsForValue().set(REDIS_RECOMMENDED_KEY, json, REDIS_CACHE_EXPIRE);
            log.info("推荐文章缓存已更新，共{}条，缓存{}分钟", voList.size(), REDIS_CACHE_EXPIRE.toMinutes());
        } catch (Exception e) {
            log.warn("推荐文章写入Redis缓存失败", e);
        }

        return Result.success(voList);
    }

    /**
     * 获取热门文章列表（优先从Redis缓存读取）
     */
    @Override
    public Result<List<HealthKnowledgeVO>> getPopularArticles() {
        // 先尝试从 Redis 缓存中获取
        try {
            String cached = redisTemplate.opsForValue().get(REDIS_POPULAR_KEY);
            if (cached != null) {
                List<HealthKnowledgeVO> voList = objectMapper.readValue(cached, new TypeReference<List<HealthKnowledgeVO>>() {});
                log.info("从缓存中获取热门文章列表，共{}条", voList.size());
                return Result.success(voList);
            }
        } catch (Exception e) {
            log.warn("从Redis读取热门文章缓存失败，回退到数据库查询", e);
        }

        // 缓存未命中，从数据库查询
        List<News> newsList = newsMapper.selectPopular();
        List<HealthKnowledgeVO> voList = convertToVOList(newsList);

        // 写入缓存
        try {
            String json = objectMapper.writeValueAsString(voList);
            redisTemplate.opsForValue().set(REDIS_POPULAR_KEY, json, REDIS_CACHE_EXPIRE);
            log.info("热门文章缓存已更新，共{}条，缓存{}分钟", voList.size(), REDIS_CACHE_EXPIRE.toMinutes());
        } catch (Exception e) {
            log.warn("热门文章写入Redis缓存失败", e);
        }

        return Result.success(voList);
    }

    /**
     * 关键词搜索文章
     */
    @Override
    public Result<List<HealthKnowledgeVO>> searchArticles(String keyword) {
        List<News> newsList = newsMapper.searchByKeyword(keyword);
        List<HealthKnowledgeVO> voList = convertToVOList(newsList);
        return Result.success(voList);
    }

    /**
     * 点赞文章
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> likeArticle(LikeRequest request) {
        Integer userId = securityUtil.getCurrentUserId();
        Integer newsId = request.getNewsId();

        // 检查文章是否存在
        News news = newsMapper.selectById(newsId);
        if (news == null) {
            throw new BusinessException(NEWS_NOT_EXIST);
        }

        // 检查是否已经点赞
        if (newsLikeMapper.existsByUserAndNews(userId, newsId)) {
            throw new BusinessException(ALREADY_LIKED);
        }

        // 插入点赞记录
        NewsLike like = new NewsLike();
        like.setUserId(userId);
        like.setNewsId(newsId);
        like.setCreateTime(LocalDateTime.now());
        newsLikeMapper.insert(like);

        // 原子增加文章点赞数
        newsMapper.incrementLikeCount(newsId);

        log.info("用户{}点赞文章{}", userId, newsId);
        return Result.success();
    }

    /**
     * 取消点赞
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> cancelLikeArticle(LikeRequest request) {
        Integer userId = securityUtil.getCurrentUserId();
        Integer newsId = request.getNewsId();

        // 检查文章是否存在
        News news = newsMapper.selectById(newsId);
        if (news == null) {
            throw new BusinessException(NEWS_NOT_EXIST);
        }

        // 检查是否存在点赞记录
        if (!newsLikeMapper.existsByUserAndNews(userId, newsId)) {
            throw new BusinessException(LIKE_NOT_EXIST);
        }

        // 删除点赞记录
        newsLikeMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<NewsLike>()
                .eq(NewsLike::getUserId, userId)
                .eq(NewsLike::getNewsId, newsId));

        // 原子减少文章点赞数
        newsMapper.decrementLikeCount(newsId);

        log.info("用户{}取消点赞文章{}", userId, newsId);
        return Result.success();
    }

    /**
     * 检查是否已点赞
     */
    @Override
    public Result<Boolean> checkIfLiked(Integer newsId) {
        Integer userId = securityUtil.getCurrentUserId();
        boolean exists = newsLikeMapper.existsByUserAndNews(userId, newsId);
        return Result.success(exists);
    }

    /**
     * 收藏文章
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> collectArticle(CollectRequest request) {
        Integer userId = securityUtil.getCurrentUserId();
        Integer newsId = request.getNewsId();

        // 检查文章是否存在
        News news = newsMapper.selectById(newsId);
        if (news == null) {
            throw new BusinessException(NEWS_NOT_EXIST);
        }

        // 检查是否已经收藏
        if (newsCollectMapper.existsByUserAndNews(userId, newsId)) {
            throw new BusinessException(ALREADY_COLLECTED);
        }

        // 插入收藏记录
        NewsCollect collect = new NewsCollect();
        collect.setUserId(userId);
        collect.setNewsId(newsId);
        collect.setCreateTime(LocalDateTime.now());
        newsCollectMapper.insert(collect);

        // 原子增加文章收藏数
        newsMapper.incrementCollectCount(newsId);

        log.info("用户{}收藏文章{}", userId, newsId);
        return Result.success();
    }

    /**
     * 取消收藏
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> cancelCollectArticle(CollectRequest request) {
        Integer userId = securityUtil.getCurrentUserId();
        Integer newsId = request.getNewsId();

        // 检查文章是否存在
        News news = newsMapper.selectById(newsId);
        if (news == null) {
            throw new BusinessException(NEWS_NOT_EXIST);
        }

        // 检查是否存在收藏记录
        if (!newsCollectMapper.existsByUserAndNews(userId, newsId)) {
            throw new BusinessException(COLLECT_NOT_EXIST);
        }

        // 删除收藏记录
        newsCollectMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<NewsCollect>()
                .eq(NewsCollect::getUserId, userId)
                .eq(NewsCollect::getNewsId, newsId));

        // 原子减少文章收藏数
        newsMapper.decrementCollectCount(newsId);

        log.info("用户{}取消收藏文章{}", userId, newsId);
        return Result.success();
    }

    /**
     * 获取用户收藏列表
     */
    @Override
    public Result<List<HealthKnowledgeVO>> getUserCollects() {
        Integer userId = securityUtil.getCurrentUserId();
        List<NewsCollect> collectList = newsCollectMapper.selectByUserId(userId);

        List<Integer> newsIds = collectList.stream()
                .map(NewsCollect::getNewsId)
                .collect(Collectors.toList());

        if (newsIds.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        List<News> newsList = newsMapper.selectBatchIds(newsIds);
        List<HealthKnowledgeVO> voList = convertToVOList(newsList);
        return Result.success(voList);
    }

    /**
     * 检查是否已收藏
     */
    @Override
    public Result<Boolean> checkIfCollected(Integer newsId) {
        Integer userId = securityUtil.getCurrentUserId();
        boolean exists = newsCollectMapper.existsByUserAndNews(userId, newsId);
        return Result.success(exists);
    }

    /**
     * 将 News 实体列表转换为 HealthKnowledgeVO 列表（批量查询点赞/收藏状态，避免N+1）
     */
    private List<HealthKnowledgeVO> convertToVOList(List<News> newsList) {
        if (newsList == null || newsList.isEmpty()) {
            return new ArrayList<>();
        }

        Integer userId = securityUtil.getCurrentUserId();
        List<Integer> newsIds = newsList.stream().map(News::getId).collect(Collectors.toList());

        // 批量查询点赞和收藏状态
        Set<Integer> likedNewsIds = userId != null ? newsLikeMapper.selectLikedNewsIds(userId, newsIds) : Collections.emptySet();
        Set<Integer> collectedNewsIds = userId != null ? newsCollectMapper.selectCollectedNewsIds(userId, newsIds) : Collections.emptySet();

        return newsList.stream().map(news -> {
            HealthKnowledgeVO vo = new HealthKnowledgeVO();
            BeanUtil.copyProperties(news, vo);
            vo.setIsLiked(userId != null && likedNewsIds.contains(news.getId()));
            vo.setIsCollected(userId != null && collectedNewsIds.contains(news.getId()));
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取用于 RAG 知识库同步的文章列表（含正文全文）
     * <p>
     * 查询所有已上架文章，支持按分类过滤。
     * 与普通列表接口不同，此方法返回正文全文 content，供向量嵌入使用。
     *
     * @param category 分类过滤（可选）
     * @return 文章完整数据列表
     */
    @Override
    public Result<List<Map<String, Object>>> getArticlesForSync(String category) {
        List<News> newsList;
        if (category != null && !category.isEmpty()) {
            newsList = newsMapper.selectByCategory(category);
        } else {
            // 查询所有已上架文章
            newsList = newsMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<News>()
                            .eq(News::getStatus, 1)
                            .orderByDesc(News::getPublishTime));
        }

        List<Map<String, Object>> result = newsList.stream().map(news -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", news.getId());
            map.put("title", news.getTitle());
            map.put("content", news.getContent());
            map.put("category", news.getCategory());
            map.put("summary", news.getSummary());
            return map;
        }).collect(Collectors.toList());

        log.info("RAG 同步查询: category={}, 共 {} 篇文章", category, result.size());
        return Result.success(result);
    }
}
