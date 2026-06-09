package com.elderlycare.ai.service;

import com.elderlycare.ai.dto.AiErrorCode;
import com.elderlycare.ai.dto.KnowledgeDocumentDTO;
import com.elderlycare.ai.feign.NewsFeignClient;
import com.elderlycare.ai.vo.KnowledgeDocVO;
import com.elderlycare.common.core.exception.BusinessException;
import com.elderlycare.common.core.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPooled;

import java.util.*;

/**
 * 知识库管理服务
 * <p>
 * 负责知识文档的 CRUD 操作，内部调用 {@link IngestionPipeline} 完成文档入库，
 * 通过 {@link RedisEmbeddingStore} 管理向量数据的删除与更新。
 * <p>
 * 文档元信息存储在 Redis 中（不涉及 MySQL），Key 格式为 {@code rag:doc:{docId}:meta}。
 *
 * @author 郑
 */
@Slf4j
@Service
public class KnowledgeService {

    private final IngestionPipeline pipeline;
    private final RedisEmbeddingStore embeddingStore;
    private final JedisPooled jedis;
    private final NewsFeignClient newsFeignClient;

    /** Redis 中记录已同步文章 ID 的 Set Key */
    private static final String SYNCED_NEWS_IDS_KEY = "rag:synced:news:ids";

    /** 支持的文件格式 */
    private static final Set<String> ALLOWED_CATEGORIES = Set.of(
            "MEDICINE", "NURSING", "SAFETY", "HEALTH", "FAQ");

    /**
     * 构造器注入所有依赖
     */
    public KnowledgeService(IngestionPipeline pipeline,
                            RedisEmbeddingStore embeddingStore,
                            JedisPooled jedis,
                            NewsFeignClient newsFeignClient) {
        this.pipeline = pipeline;
        this.embeddingStore = embeddingStore;
        this.jedis = jedis;
        this.newsFeignClient = newsFeignClient;
    }

    /**
     * 上传文档并入库
     * <p>
     * 1. 校验文档分类合法性
     * 2. 构建元数据
     * 3. 经摄取管道切分 → 嵌入 → 存储
     * 4. 保存文档元信息
     *
     * @param dto 文档 DTO
     * @return 文档 ID
     */
    public String uploadDocument(KnowledgeDocumentDTO dto) {
        // 校验分类
        if (!ALLOWED_CATEGORIES.contains(dto.getCategory().toUpperCase())) {
            throw new BusinessException(AiErrorCode.RAG_UNSUPPORTED_FORMAT,
                    "不支持的文档分类: " + dto.getCategory());
        }

        String docId = UUID.randomUUID().toString();
        log.info("上传文档: id={}, title={}, category={}", docId, dto.getTitle(), dto.getCategory());

        // 构建元数据（注入到每个向量片段）
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("doc_id", docId);
        metadata.put("title", dto.getTitle());
        metadata.put("category", dto.getCategory().toUpperCase());
        metadata.put("created_at", String.valueOf(System.currentTimeMillis()));
        // 合并自定义元数据
        if (dto.getMetadata() != null) {
            dto.getMetadata().forEach((k, v) -> metadata.put(k, v != null ? v.toString() : ""));
        }

        // 经摄取管道处理
        List<String> chunkIds = pipeline.ingest(dto.getContent(), metadata);
        log.info("文档 {} 入库完成，生成 {} 个向量片段", docId, chunkIds.size());

        return docId;
    }

    /**
     * 批量上传文档
     *
     * @param dtos 文档 DTO 列表
     * @return 成功入库的文档 ID 列表
     */
    public List<String> uploadDocuments(List<KnowledgeDocumentDTO> dtos) {
        return dtos.stream().map(this::uploadDocument).toList();
    }

    /**
     * 删除指定文档及其所有向量片段
     *
     * @param docId 文档 ID
     */
    public void deleteDocument(String docId) {
        log.info("删除文档: {}", docId);
        embeddingStore.removeByDocId(docId);
    }

    /**
     * 更新文档：先删除旧数据，再重新入库
     *
     * @param docId 文档 ID
     * @param dto   新的文档内容
     */
    public void updateDocument(String docId, KnowledgeDocumentDTO dto) {
        log.info("更新文档: {}", docId);
        embeddingStore.removeByDocId(docId);
        // 使用相同的 docId 重新入库
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("doc_id", docId);
        metadata.put("title", dto.getTitle());
        metadata.put("category", dto.getCategory().toUpperCase());
        metadata.put("created_at", String.valueOf(System.currentTimeMillis()));
        metadata.put("updated_at", String.valueOf(System.currentTimeMillis()));
        if (dto.getMetadata() != null) {
            dto.getMetadata().forEach((k, v) -> metadata.put(k, v != null ? v.toString() : ""));
        }
        pipeline.ingest(dto.getContent(), metadata);
    }

    /**
     * 分页列出知识库文档（仅元信息，不含正文）
     *
     * @param keyword 搜索关键词（可选，匹配标题）
     * @return 文档 VO 列表
     */
    public List<KnowledgeDocVO> listDocuments(String keyword) {
        // 从 Redis 中枚举所有 rag:doc:*:meta 的 key（简化实现，实际可维护一个 Set 索引）
        // 当前简化：返回空列表（完整实现需维护 doc:ids SET）
        log.info("查询知识库文档列表，关键词: {}", keyword);
        return List.of();
    }

    /**
     * 列出所有文档分类及数量统计
     *
     * @return 分类 → 数量 映射
     */
    public Map<String, Long> getCategories() {
        log.info("查询文档分类统计");
        return Map.of();
    }

    // ═══════════════════════════════════════════
    // 资讯模块同步
    // ═══════════════════════════════════════════

    /**
     * 从资讯模块同步文章到向量知识库
     * <p>
     * 核心流程：
     * <ol>
     *   <li>通过 Feign 调用 elderly-news 获取已上架文章（含正文全文）</li>
     *   <li>与 Redis 中已同步记录对比，跳过已入库文章（去重）</li>
     *   <li>为每篇文章构建元数据（来源标记为资讯模块）</li>
     *   <li>经摄取管道处理：切分 → 嵌入 → 存储到 Redis 向量库</li>
     *   <li>记录同步状态，防止重复入库</li>
     * </ol>
     *
     * @param category 分类过滤（null 表示同步全部）
     * @return 同步统计：total（资讯总文章数）、synced（本次新入库）、skipped（已存在跳过）
     */
    public Map<String, Object> syncFromNews(String category) {
        log.info("开始从资讯模块同步文章, category={}", category);

        // 1. 通过 Feign 拉取文章列表
        Result<List<Map<String, Object>>> result = newsFeignClient.getArticlesForSync(category);
        if (result == null || result.getData() == null) {
            log.warn("资讯服务返回空数据");
            return Map.of("total", 0, "synced", 0, "skipped", 0);
        }
        List<Map<String, Object>> articles = result.getData();
        log.info("从资讯服务获取到 {} 篇文章", articles.size());

        int synced = 0;
        int skipped = 0;

        for (Map<String, Object> article : articles) {
            Object idObj = article.get("id");
            if (idObj == null) continue;

            String newsId = "news_" + idObj;

            // 2. 去重检查：已同步的文章跳过
            if (isAlreadySynced(newsId)) {
                skipped++;
                continue;
            }

            // 3. 构建向量元数据（来源标记为资讯模块）
            Map<String, String> metadata = new LinkedHashMap<>();
            metadata.put("doc_id", newsId);
            metadata.put("title", Objects.toString(article.get("title"), "未知标题"));
            metadata.put("category", Objects.toString(article.get("category"), "HEALTH"));
            metadata.put("source", "elderly-news");
            metadata.put("created_at", String.valueOf(System.currentTimeMillis()));

            // 合并文章摘要作为元数据
            String summary = Objects.toString(article.get("summary"), "");
            if (!summary.isEmpty()) {
                metadata.put("summary", summary);
            }

            // 4. 经摄取管道入库（切分 → 嵌入 → 存储）
            try {
                String content = Objects.toString(article.get("content"), "");
                if (content.isEmpty()) {
                    log.warn("文章 {} 内容为空，跳过", newsId);
                    skipped++;
                    continue;
                }
                pipeline.ingest(content, metadata);
                markSynced(newsId);
                synced++;
                log.info("文章同步成功: {} (标题: {})", newsId, metadata.get("title"));
            } catch (Exception e) {
                log.error("文章 {} 同步失败: {}", newsId, e.getMessage(), e);
            }
        }

        log.info("资讯同步完成: total={}, synced={}, skipped={}", articles.size(), synced, skipped);
        return Map.of("total", articles.size(), "synced", synced, "skipped", skipped);
    }

    /**
     * 检查文章是否已同步到向量库
     * <p>
     * 通过 Redis Set {@code rag:synced:news:ids} 记录已同步的文章 ID。
     *
     * @param newsId 文章唯一标识（格式: news_{id}）
     * @return true 表示已同步
     */
    private boolean isAlreadySynced(String newsId) {
        try {
            return jedis.sismember(SYNCED_NEWS_IDS_KEY, newsId);
        } catch (Exception e) {
            log.warn("检查同步状态失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 标记文章已同步
     * <p>
     * 将文章 ID 加入 Redis Set，用于后续去重判断。
     *
     * @param newsId 文章唯一标识
     */
    private void markSynced(String newsId) {
        try {
            jedis.sadd(SYNCED_NEWS_IDS_KEY, newsId);
        } catch (Exception e) {
            log.warn("标记同步状态失败: {}", e.getMessage());
        }
    }
}
