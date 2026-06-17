package com.elderlycare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.common.exception.BusinessException;
import com.elderlycare.common.exception.ErrorCode;
import com.elderlycare.mapper.NewsMapper;
import com.elderlycare.pojo.dto.news.NewsAddRequest;
import com.elderlycare.pojo.dto.news.NewsUpdateRequest;
import com.elderlycare.pojo.entity.News;
import com.elderlycare.pojo.vo.admin.AdminNewsVO;
import com.elderlycare.pojo.vo.PageResult;
import com.elderlycare.service.AdminNewsService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminNewsServiceImpl implements AdminNewsService {

    private final NewsMapper newsMapper;

    private static final String CACHE_RECOMMENDED = "NEW_RECOMMENDED";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer publishNews(NewsAddRequest request, Integer creatorId) {
        News news = new News();
        BeanUtils.copyProperties(request, news);
        news.setViewCount(0);
        news.setCreatorId(creatorId);
        news.setStatus(1);
        news.setPublishTime(LocalDateTime.now());

        newsMapper.insert(news);
        log.info("发布资讯成功，id: {}, title: {}", news.getId(), news.getTitle());
        return news.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateNews(NewsUpdateRequest request) {
        News news = newsMapper.selectById(request.getId());
        if (news == null) {
            log.error("资讯不存在，id: {}", request.getId());
            throw new BusinessException(ErrorCode.NOT_FOUND, "资讯不存在");
        }

        news.setTitle(request.getTitle());
        news.setContent(request.getContent());
        news.setSummary(request.getSummary());
        news.setCoverImage(request.getCoverImage());
        news.setCategory(request.getCategory());

        boolean recommendedChanged = false;
        if (request.getIsRecommended() != null) {
            recommendedChanged = !request.getIsRecommended().equals(news.getIsRecommended());
            news.setIsRecommended(request.getIsRecommended());
        }

        if (request.getStatus() != null) {
            news.setStatus(request.getStatus());
            if (request.getStatus() == 1 && news.getPublishTime() == null) {
                news.setPublishTime(LocalDateTime.now());
            }
        }

        newsMapper.updateById(news);
        if (recommendedChanged) {
            stringRedisTemplate.delete(CACHE_RECOMMENDED);
        }
        log.info("更新资讯成功，id: {}", request.getId());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteNews(Integer newsId) {
        int rows = newsMapper.deleteById(newsId);
        if (rows > 0) {
            stringRedisTemplate.delete(CACHE_RECOMMENDED);
        }
        log.info("删除资讯成功，id: {}", newsId);
        return rows > 0;
    }

    @Override
    public PageResult<AdminNewsVO> queryNews(Integer page, Integer size, String category, Integer status) {
        LambdaQueryWrapper<News> wrapper = new LambdaQueryWrapper<>();

        if (category != null && !category.trim().isEmpty()) {
            wrapper.eq(News::getCategory, category);
        }

        if (status != null) {
            wrapper.eq(News::getStatus, status);
        }

        wrapper.orderByDesc(News::getPublishTime);

        Page<News> newsPage = newsMapper.selectPage(new Page<>(page, size), wrapper);

        var newsVOList = newsPage.getRecords().stream()
                .map(this::convertToAdminNewsVO)
                .toList();

        return PageResult.of(newsVOList, newsPage.getTotal(), page, size);
    }

    @Override
    public AdminNewsVO getNewsDetail(Integer newsId) {
        News news = newsMapper.selectById(newsId);
        if (news == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "资讯不存在");
        }
        return convertToAdminNewsVO(news);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changeNewsStatus(Integer newsId, Integer status) {
        News news = newsMapper.selectById(newsId);
        if (news == null) {
            log.error("资讯不存在，id: {}", newsId);
            throw new BusinessException(ErrorCode.NOT_FOUND, "资讯不存在");
        }

        news.setStatus(status);
        if (status == 1 && news.getPublishTime() == null) {
            news.setPublishTime(LocalDateTime.now());
        }

        newsMapper.updateById(news);
        log.info("修改资讯状态成功，id: {}, status: {}", newsId, status);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changeNewsRecommended(Integer newsId, Integer isRecommended) {
        News news = newsMapper.selectById(newsId);
        if (news == null) {
            log.error("资讯不存在，id: {}", newsId);
            throw new BusinessException(ErrorCode.NOT_FOUND, "资讯不存在");
        }

        news.setIsRecommended(isRecommended);
        newsMapper.updateById(news);
        stringRedisTemplate.delete(CACHE_RECOMMENDED);
        log.info("修改资讯推荐状态成功，id: {}, isRecommended: {}", newsId, isRecommended);
        return true;
    }

    private AdminNewsVO convertToAdminNewsVO(News news) {
        return new AdminNewsVO(
                news.getId(),
                news.getTitle(),
                news.getSummary(),
                news.getCategory(),
                news.getCoverImage(),
                news.getViewCount(),
                news.getIsRecommended(),
                news.getIsRecommended() == 1 ? "推荐" : "普通",
                news.getStatus(),
                getStatusDesc(news.getStatus()),
                news.getCreatorId(),
                news.getPublishTime()
        );
    }

    private String getStatusDesc(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "草稿";
            case 1 -> "已发布";
            case 2 -> "下架";
            default -> "未知";
        };
    }
}