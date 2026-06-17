package com.elderlycare.service;

import com.elderlycare.pojo.dto.news.NewsAddRequest;
import com.elderlycare.pojo.dto.news.NewsUpdateRequest;
import com.elderlycare.pojo.vo.admin.AdminNewsVO;
import com.elderlycare.pojo.vo.PageResult;

/**
 * 资讯管理服务接口
 */
public interface AdminNewsService {

    /**
     * 发布资讯
     *
     * @param request 发布请求
     * @param creatorId 创建者 ID
     * @return 资讯 ID
     */
    Integer publishNews(NewsAddRequest request, Integer creatorId);

    /**
     * 更新资讯
     *
     * @param request 更新请求
     * @return 操作结果
     */
    boolean updateNews(NewsUpdateRequest request);

    /**
     * 删除资讯
     *
     * @param newsId 资讯 ID
     * @return 操作结果
     */
    boolean deleteNews(Integer newsId);

    /**
     * 查询资讯列表（分页）
     *
     * @param page 页码
     * @param size 每页条数
     * @param category 分类筛选
     * @param status 状态筛选
     * @return 分页结果
     */
    PageResult<AdminNewsVO> queryNews(Integer page, Integer size, String category, Integer status);

    /**
     * 获取资讯详情
     *
     * @param newsId 资讯 ID
     * @return 资讯详情
     */
    AdminNewsVO getNewsDetail(Integer newsId);

    /**
     * 上架/下架资讯
     *
     * @param newsId 资讯 ID
     * @param status 新状态
     * @return 操作结果
     */
    boolean changeNewsStatus(Integer newsId, Integer status);

    /**
     * 推荐/取消推荐资讯
     *
     * @param newsId 资讯 ID
     * @param isRecommended 是否推荐
     * @return 操作结果
     */
    boolean changeNewsRecommended(Integer newsId, Integer isRecommended);
}
