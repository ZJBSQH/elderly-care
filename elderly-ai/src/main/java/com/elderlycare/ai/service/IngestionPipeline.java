package com.elderlycare.ai.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 文档摄取管道
 * <p>
 * 将原始文档经过"切分 → 嵌入 → 存储"三阶段处理后写入 Redis 向量库。
 * 切分采用递归字符切分策略（段落 → 句子 → 字符），相邻片段有重叠以保证语义连贯。
 * 嵌入模型使用千问 DashScope text-embedding-v3。
 *
 * @author 郑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionPipeline {

    /** 千问嵌入模型（DashScope text-embedding-v3），由 LangChain4j 自动配置 */
    private final EmbeddingModel embeddingModel;

    /** Redis 向量存储 */
    private final RedisEmbeddingStore embeddingStore;

    /** 切分最大字符数 */
    @Value("${elderly.rag.chunk.max-size:500}")
    private int maxChunkSize;

    /** 相邻片段重叠字符数 */
    @Value("${elderly.rag.chunk.max-overlap:50}")
    private int maxOverlap;

    /**
     * 完整摄取管道：文档 → 切分 → 嵌入 → 存储
     *
     * @param text     原始文本内容
     * @param metadata 元数据（title, category, doc_id 等）
     * @return 生成的片段 ID 列表
     */
    public List<String> ingest(String text, Map<String, String> metadata) {
        log.info("开始文档摄取: title={}, 正文长度={} 字符",
                metadata.getOrDefault("title", "未知"), text.length());

        // 阶段1: 文档切分
        List<TextSegment> segments = split(text, metadata);
        log.info("切分完成，共 {} 个片段", segments.size());

        // 阶段2: 批量嵌入
        List<Embedding> embeddings = embed(segments);
        log.info("嵌入完成，共 {} 个向量", embeddings.size());

        // 阶段3: 向量存储
        List<String> ids = embeddingStore.addAll(embeddings, segments);
        log.info("存储完成，生成 {} 条向量记录", ids.size());

        return ids;
    }

    /**
     * 阶段1: 文档切分
     * <p>
     * 使用递归字符切分器（DocumentSplitters.recursive）：先按段落分，段落过长则按句子分，
     * 句子仍过长则按字符截断。相邻片段保留重叠区域，避免关键信息被切断。
     * <p>
     * 切分完成后为每个片段注入元数据（标题、分类、doc_id 等），便于检索时溯源。
     */
    List<TextSegment> split(String text, Map<String, String> metadata) {
        // 递归切分器（仅需两个参数：块大小 + 重叠大小）
        var splitter = DocumentSplitters.recursive(maxChunkSize, maxOverlap);

        // 文档 → 切分成片段
        List<TextSegment> segments = splitter.split(Document.from(text));

        // 为每个片段注入元数据 + 序号
        for (int i = 0; i < segments.size(); i++) {
            TextSegment seg = segments.get(i);
            Metadata segMeta = seg.metadata();
            // 注入业务元数据
            metadata.forEach((k, v) -> {
                if (v != null) segMeta.put(k, v);
            });
            segMeta.put("chunk_index", i);
        }

        return segments;
    }

    /**
     * 阶段2: 向量嵌入
     * <p>
     * 调用千问 DashScope embedding API 将文本片段批量转为 1536 维向量。
     * embedAll 接受 List&lt;TextSegment&gt;，返回 Response&lt;List&lt;Embedding&gt;&gt;。
     */
    List<Embedding> embed(List<TextSegment> segments) {
        return embeddingModel.embedAll(segments).content();
    }
}
