package com.elderlycare.ai.controller;

import com.elderlycare.ai.dto.KnowledgeDocumentDTO;
import com.elderlycare.ai.service.KnowledgeService;
import com.elderlycare.ai.vo.KnowledgeDocVO;
import com.elderlycare.common.core.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 知识库管理控制器
 * <p>
 * 提供医学知识文档的上传、删除、更新、查询等管理接口。
 * 仅限管理员操作，普通用户通过 RAG 问答接口消费知识库内容。
 *
 * @author 郑
 */
@Slf4j
@RequestMapping("/ai/kb")
@RestController
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeService knowledgeService;

    /**
     * 上传单篇文档
     *
     * @param dto 文档 DTO（标题 + 分类 + 正文 + 可选元数据）
     * @return 文档 ID
     */
    @PostMapping("/document")
    public Result<String> uploadDocument(@Valid @RequestBody KnowledgeDocumentDTO dto) {
        log.info("上传知识文档: title={}, category={}", dto.getTitle(), dto.getCategory());
        String docId = knowledgeService.uploadDocument(dto);
        return Result.success(docId, "文档入库成功");
    }

    /**
     * 批量上传文档
     *
     * @param dtos 文档 DTO 列表
     * @return 成功入库的文档 ID 列表
     */
    @PostMapping("/documents/batch")
    public Result<List<String>> uploadDocuments(@Valid @RequestBody List<KnowledgeDocumentDTO> dtos) {
        log.info("批量上传知识文档，数量: {}", dtos.size());
        List<String> docIds = knowledgeService.uploadDocuments(dtos);
        return Result.success(docIds, "批量入库成功");
    }

    /**
     * 删除指定文档及其所有向量片段
     *
     * @param id 文档 ID
     */
    @DeleteMapping("/document/{id}")
    public Result<Void> deleteDocument(@PathVariable String id) {
        log.info("删除知识文档: {}", id);
        knowledgeService.deleteDocument(id);
        return Result.success(null, "文档删除成功");
    }

    /**
     * 更新文档（删除旧数据 + 重新入库）
     *
     * @param id  文档 ID
     * @param dto 新的文档内容
     */
    @PutMapping("/document/{id}")
    public Result<Void> updateDocument(@PathVariable String id,
                                        @Valid @RequestBody KnowledgeDocumentDTO dto) {
        log.info("更新知识文档: {}", id);
        knowledgeService.updateDocument(id, dto);
        return Result.success(null, "文档更新成功");
    }

    /**
     * 分页列出知识库文档元数据
     *
     * @param keyword 搜索关键词（可选，按标题模糊匹配）
     * @return 文档 VO 列表
     */
    @GetMapping("/documents")
    public Result<List<KnowledgeDocVO>> listDocuments(
            @RequestParam(required = false) String keyword) {
        log.info("查询知识库文档列表，关键词: {}", keyword);
        List<KnowledgeDocVO> docs = knowledgeService.listDocuments(keyword);
        return Result.success(docs);
    }

    /**
     * 列出所有文档分类及数量统计
     *
     * @return 分类 → 数量 映射
     */
    @GetMapping("/categories")
    public Result<Map<String, Long>> getCategories() {
        log.info("查询文档分类统计");
        Map<String, Long> categories = knowledgeService.getCategories();
        return Result.success(categories);
    }

    /**
     * 从资讯模块同步文章到 RAG 向量知识库
     * <p>
     * 一键拉取 elderly-news 中所有已上架的健康知识文章，
     * 经切分、嵌入后写入 Redis 向量库。已同步的文章会自动跳过（去重）。
     * <p>
     * 典型用法：管理员在资讯模块发布新文章后，调用此接口刷新知识库。
     *
     * @param category 分类过滤（可选，如 MEDICINE、HEALTH，不传则同步全部）
     * @return 同步统计：total（资讯总文章数）、synced（本次新入库）、skipped（已存在跳过）
     */
    @PostMapping("/sync-from-news")
    public Result<Map<String, Object>> syncFromNews(
            @RequestParam(required = false) String category) {
        log.info("收到资讯同步请求, category={}", category);
        Map<String, Object> stats = knowledgeService.syncFromNews(category);
        return Result.success(stats, "同步完成");
    }
}
