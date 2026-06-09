package com.elderlycare.ai.feign;

import com.elderlycare.common.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 资讯服务 Feign 客户端
 * <p>
 * 通过声明式 HTTP 调用 elderly-news 服务，
 * 获取已上架的健康知识文章，用于同步到 RAG 向量知识库。
 *
 * @author 郑
 */
@FeignClient(name = "elderly-news", configuration = com.elderlycare.ai.config.FeignConfig.class)
public interface NewsFeignClient {

    /**
     * 获取用于 RAG 同步的文章列表（含正文全文）
     * <p>
     * 返回所有已上架（status=1）文章，支持按分类过滤。
     * 每条记录包含 id、title、content、category、summary。
     *
     * @param category 分类过滤（可选，null 表示全部）
     * @return 文章完整数据列表
     */
    @GetMapping("/health-knowledge/articles/sync")
    Result<List<Map<String, Object>>> getArticlesForSync(
            @RequestParam(value = "category", required = false) String category);
}
